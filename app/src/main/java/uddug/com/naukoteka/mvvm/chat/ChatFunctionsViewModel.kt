package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

@HiltViewModel
class ChatFunctionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    fun markUnread(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.setDialogUnread(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    fun pinChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.pinDialog(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    fun disableNotifications(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.disableNotifications(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    fun enableNotifications(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.enableNotifications(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    fun selectMessages(dialogId: Long) {
        // TODO: implement when API is available
    }

    fun blockChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.blockDialog(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }

    fun deleteChat(dialogId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteDialog(dialogId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }
}
