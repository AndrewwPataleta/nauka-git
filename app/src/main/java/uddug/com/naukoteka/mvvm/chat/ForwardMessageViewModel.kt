package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.naukoteka.ui.chat.di.SocketService

@HiltViewModel
class ForwardMessageViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForwardMessageUiState())
    val uiState: StateFlow<ForwardMessageUiState> = _uiState

    private val _events = MutableSharedFlow<ForwardMessageEvent>()
    val events: SharedFlow<ForwardMessageEvent> = _events.asSharedFlow()

    init {
        socketService.connect()
        loadDialogs()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun loadDialogs() {
        viewModelScope.launch {
            val dialogs = try {
                chatInteractor.getDialogs()
            } catch (e: Exception) {
                emptyList()
            }
            val items = dialogs.map { it.toForwardMessageItem() }
                .distinctBy { it.dialogId }
            _uiState.update { state ->
                state.copy(dialogs = items)
            }
        }
    }

    fun forwardMessage(messageId: Long, dialogId: Long) {
        viewModelScope.launch {
            try {
                val message = ChatSocketMessage(
                    dialog = dialogId,
                    text = "",
                    forwarded = messageId
                )
                socketService.sendMessage("message", message)
                _events.emit(ForwardMessageEvent.ForwardSuccess(dialogId))
            } catch (e: Exception) {
                _events.emit(ForwardMessageEvent.ForwardError)
            }
        }
    }

    private fun Chat.toForwardMessageItem(): ForwardMessageItem {
        val title = dialogName.takeIf { it.isNotBlank() }
            ?: interlocutor.fullName
            ?: interlocutor.nickname
            ?: ""
        val subtitle = lastMessage.text?.takeIf { it.isNotBlank() }
        return ForwardMessageItem(
            dialogId = dialogId,
            title = title,
            subtitle = subtitle,
            avatarUrl = interlocutor.image
        )
    }
}

data class ForwardMessageUiState(
    val query: String = "",
    val dialogs: List<ForwardMessageItem> = emptyList(),
)

data class ForwardMessageItem(
    val dialogId: Long,
    val title: String,
    val subtitle: String?,
    val avatarUrl: String?,
)

sealed class ForwardMessageEvent {
    data class ForwardSuccess(val dialogId: Long) : ForwardMessageEvent()
    object ForwardError : ForwardMessageEvent()
}
