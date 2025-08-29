package uddug.com.naukoteka.mvvm.chat

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
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import javax.inject.Inject
import io.reactivex.Single
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class ChatCreateSingleViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatCreateSingleUiState>(ChatCreateSingleUiState.Loading)
    val uiState: StateFlow<ChatCreateSingleUiState> = _uiState

    private val _events = MutableSharedFlow<ChatCreateSingleEvent>()
    val events: SharedFlow<ChatCreateSingleEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                val users = withContext(Dispatchers.IO) {
                    chatInteractor.getDialogs()
                        .filter { it.dialogType == 1 }
                        .map { chat ->
                            UserProfileFullInfo(
                                id = chat.interlocutor.userId,
                                fullName = chat.interlocutor.fullName,
                                image = Image(path = chat.interlocutor.image)
                            )
                        }
                }
                _uiState.value = ChatCreateSingleUiState.Success(
                    query = "",
                    users = users,
                    searchResults = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onGroupCreateClick() {
        viewModelScope.launch {
            _events.emit(ChatCreateSingleEvent.OpenMultiCreate)
        }
    }

    fun onCurrentSearchChange(query: String) {
        val currentState = _uiState.value
        if (currentState is ChatCreateSingleUiState.Success) {
            _uiState.value = currentState.copy(query = query)
            viewModelScope.launch {
                if (query.isNotBlank()) {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            chatInteractor.searchUsers(query)
                        }
                        val success = _uiState.value as? ChatCreateSingleUiState.Success
                        if (success != null) {
                            _uiState.value = success.copy(searchResults = result)
                        }
                    } catch (e: Exception) {
                        _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
                    }
                } else {
                    val success = _uiState.value as? ChatCreateSingleUiState.Success
                    if (success != null) {
                        _uiState.value = success.copy(searchResults = emptyList())
                    }
                }
            }
        } else {
            _uiState.value = ChatCreateSingleUiState.Success(
                query = query,
                users = emptyList(),
                searchResults = emptyList()
            )
        }
    }

    fun onUserClick(userId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val selectedUser =
                if (currentState is ChatCreateSingleUiState.Success) {
                    (currentState.users + currentState.searchResults).find { it.id == userId }
                } else null

            // 👉 Берём профиль текущего пользователя из UserProfileRepository
            val me = withContext(Dispatchers.IO) {
                userProfileRepository.getProfileInfo().await()
            }

            val hasPermit = selectedUser?.permits?.contains("82:200") ?: true
            if (hasPermit) {
                _events.emit(ChatCreateSingleEvent.OpenDialogDetail(userId))
            } else {
                _uiState.value = ChatCreateSingleUiState.Error("Нет разрешения на личный диалог")
            }

//            val selectedLastName =
//                selectedUser?.lastName ?: selectedUser?.fullName?.split(" ")?.firstOrNull()
//
//            val dialogName = listOfNotNull(me.lastName, selectedLastName).joinToString(" ")
//
//            // роли участников
//            val userRoles = mutableMapOf<String, String?>()
//            me.id?.let { userRoles[it] = "37:202" }
//            userRoles[userId] = "37:202"
//
//            val dialogId = withContext(Dispatchers.IO) {
//                chatInteractor.createDialog(dialogName, userRoles)
//            }
//
//            _events.emit(ChatCreateSingleEvent.OpenDialogDetail(dialogId))
        }
    }
}

/**
 * Простая корутинная обёртка для RxJava Single<T>,
 * чтобы не использовать blockingGet() и не блокировать поток.
 */
private suspend fun <T> Single<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        val d = this.subscribe(
            { value -> if (cont.isActive) cont.resume(value) },
            { error -> if (cont.isActive) cont.resumeWithException(error) }
        )
        cont.invokeOnCancellation { d.dispose() }
    }

sealed class ChatCreateSingleEvent {
    data class OpenDialogDetail(val interlocutorId: String) : ChatCreateSingleEvent()
    object OpenMultiCreate : ChatCreateSingleEvent()
}

sealed class ChatCreateSingleUiState {
    object Loading : ChatCreateSingleUiState()
    data class Success(
        val query: String,
        val users: List<UserProfileFullInfo>,
        val searchResults: List<UserProfileFullInfo>,
    ) : ChatCreateSingleUiState()

    data class Error(val message: String) : ChatCreateSingleUiState()
}
