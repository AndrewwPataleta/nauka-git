package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

@HiltViewModel
class ChatFunctionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<ChatFunctionsEvent>()
    val events: SharedFlow<ChatFunctionsEvent> = _events.asSharedFlow()

    fun markUnread(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.setDialogUnread(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun pinChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.pinDialog(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun unpinChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.unpinDialog(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun disableNotifications(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.disableNotifications(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun enableNotifications(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.enableNotifications(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun selectMessages(dialogId: Long) {
        
    }

    fun blockChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.blockDialog(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun unblockChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.unblockDialog(dialogId)
            } catch (e: Exception) {
                
            }
        }
    }

    fun deleteChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteDialog(dialogId)
                _events.emit(ChatFunctionsEvent.ChatDeleted)
            } catch (e: Exception) {
                
            }
        }
    }
}

sealed class ChatFunctionsEvent {
    data object ChatDeleted : ChatFunctionsEvent()
}
