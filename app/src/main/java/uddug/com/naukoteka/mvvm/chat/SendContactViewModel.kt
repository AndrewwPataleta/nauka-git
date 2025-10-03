package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode
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
    private val chatInteractor: ChatInteractor,
    private val chatStatusFormatter: ChatStatusFormatter
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
        return if (status.isOnline) {
            chatStatusFormatter.online(ChatStatusTextMode.CONTACT)
        } else {
            status.lastSeen?.let { lastSeen ->
                runCatching { Instant.parse(lastSeen) }
                    .map { instant -> chatStatusFormatter.formatLastSeen(instant, ChatStatusTextMode.CONTACT) }
                    .getOrNull()
            }
        }
    }
}

