package uddug.com.naukoteka.mvvm.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import javax.inject.Inject

// Экран «Чужой профиль». Тот же профиль, что и свой (GET core/user_profile/:id),
// но read-only + кнопка «Подписаться» (PATCH core/user_subscription).
@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userUUIDCache: UserUUIDCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow<OtherProfileUiState>(OtherProfileUiState.Loading)
    val uiState: StateFlow<OtherProfileUiState> = _uiState

    private val _events = MutableSharedFlow<OtherProfileEvent>()
    val events: SharedFlow<OtherProfileEvent> = _events.asSharedFlow()

    private var userId: String? = null
    private var subscribeInFlight = false

    // true, если открыли самого себя — тогда прячем «Подписаться»/сообщение.
    fun isSelf(id: String): Boolean = id == userUUIDCache.entity

    fun load(userId: String) {
        this.userId = userId
        _uiState.value = OtherProfileUiState.Loading
        viewModelScope.launch {
            try {
                val profile = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo(userId).blockingGet()
                }
                _uiState.value = OtherProfileUiState.Success(
                    profile = profile,
                    isSelf = userId == userUUIDCache.entity,
                    feed = emptyList(),
                    feedLoading = true,
                )
                loadFeed(userId)
            } catch (e: Exception) {
                _uiState.value = OtherProfileUiState.Error(e.message ?: "Не удалось загрузить профиль")
            }
        }
    }

    private fun loadFeed(userId: String) {
        viewModelScope.launch {
            val feed = try {
                withContext(Dispatchers.IO) {
                    userProfileRepository.getUserFeed(userId).blockingGet()
                }
            } catch (e: Exception) {
                emptyList()
            }
            val current = _uiState.value as? OtherProfileUiState.Success ?: return@launch
            _uiState.value = current.copy(feed = feed, feedLoading = false)
        }
    }

    fun onSubscribeClick() {
        val current = _uiState.value as? OtherProfileUiState.Success ?: return
        val uref = current.profile.uref
        if (uref.isNullOrEmpty() || subscribeInFlight) return

        val target = !current.isSubscribed
        // Оптимистично отражаем в UI и откатываем при ошибке.
        _uiState.value = current.copy(
            profile = current.profile.copy(
                feedState = (current.profile.feedState ?: uddug.com.domain.entities.profile.FeedState())
                    .copy(subscribed = target),
                meta = current.profile.meta?.copy(
                    subscnCount = ((current.profile.meta?.subscnCount ?: 0) + if (target) 1 else -1)
                        .coerceAtLeast(0)
                )
            )
        )
        subscribeInFlight = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userProfileRepository.setSubscribed(uref, target).blockingAwait()
                }
            } catch (e: Exception) {
                // Откат.
                val roll = _uiState.value as? OtherProfileUiState.Success
                if (roll != null) {
                    _uiState.value = roll.copy(
                        profile = roll.profile.copy(
                            feedState = (roll.profile.feedState ?: uddug.com.domain.entities.profile.FeedState())
                                .copy(subscribed = !target),
                            meta = roll.profile.meta?.copy(
                                subscnCount = ((roll.profile.meta?.subscnCount ?: 0) + if (target) -1 else 1)
                                    .coerceAtLeast(0)
                            )
                        )
                    )
                }
                _events.emit(OtherProfileEvent.ShowError("Не удалось изменить подписку"))
            } finally {
                subscribeInFlight = false
            }
        }
    }

    fun onMessageClick() {
        val id = userId ?: return
        viewModelScope.launch { _events.emit(OtherProfileEvent.OpenDialog(id)) }
    }

    fun onMoreClick() {
        viewModelScope.launch { _events.emit(OtherProfileEvent.OpenMore) }
    }
}

sealed class OtherProfileEvent {
    data class OpenDialog(val interlocutorId: String) : OtherProfileEvent()
    object OpenMore : OtherProfileEvent()
    data class ShowError(val message: String) : OtherProfileEvent()
}

sealed class OtherProfileUiState {
    object Loading : OtherProfileUiState()
    data class Success(
        val profile: UserProfileFullInfo,
        val isSelf: Boolean,
        val feed: List<FeedContainer>,
        val feedLoading: Boolean,
    ) : OtherProfileUiState() {
        val isSubscribed: Boolean get() = profile.feedState?.subscribed == true
    }

    data class Error(val message: String) : OtherProfileUiState()
}
