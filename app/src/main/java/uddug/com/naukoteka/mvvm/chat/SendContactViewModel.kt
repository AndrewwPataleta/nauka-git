package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

data class SendContactUiState(
    val query: String = "",
    val contacts: List<SendContactItem> = emptyList()
)

data class SendContactItem(
    val user: UserProfileFullInfo,
    val status: String?
)

@HiltViewModel
class SendContactViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendContactUiState())
    val uiState: StateFlow<SendContactUiState> = _uiState

    init {
        loadContacts()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            val contacts = try {
                val dialogs = chatInteractor.getDialogs()
                    .filter { it.dialogType == 1 }

                val userIds = dialogs.mapNotNull { it.interlocutor.userId }.distinct()
                val statuses = if (userIds.isNotEmpty()) {
                    try {
                        chatInteractor.getUsersStatus(userIds)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                val statusMap = statuses.associateBy { it.userId }

                dialogs.mapNotNull { chat ->
                    val interlocutor = chat.interlocutor
                    val userId = interlocutor.userId ?: return@mapNotNull null
                    val fullName = interlocutor.fullName ?: interlocutor.nickname
                    val profile = UserProfileFullInfo(
                        id = userId,
                        fullName = fullName,
                        nickname = interlocutor.nickname,
                        image = Image(path = interlocutor.image)
                    )
                    SendContactItem(
                        user = profile,
                        status = formatStatus(statusMap[userId])
                    )
                }.distinctBy { it.user.id }
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(contacts = contacts) }
        }
    }

    private fun formatStatus(status: UserStatus?): String? {
        status ?: return null
        if (status.isOnline) {
            return "Сейчас онлайн"
        }
        val lastSeen = status.lastSeen ?: return null
        return try {
            val instant = Instant.parse(lastSeen)
            val duration = Duration.between(instant, Instant.now())
            val minutes = duration.toMinutes().coerceAtLeast(1)
            val hours = duration.toHours().coerceAtLeast(1)
            val days = duration.toDays().coerceAtLeast(1)
            val weeks = (days / 7).coerceAtLeast(1)
            when {
                minutes < 60 -> "Был(а) в сети ${minutes} мин. назад"
                hours < 24 -> "Был(а) в сети ${hours} ч. назад"
                days == 1L -> "Был(а) в сети вчера"
                days < 7 -> "Был(а) в сети ${days} д. назад"
                else -> "Был(а) в сети ${weeks} нед. назад"
            }
        } catch (e: Exception) {
            null
        }
    }
}

