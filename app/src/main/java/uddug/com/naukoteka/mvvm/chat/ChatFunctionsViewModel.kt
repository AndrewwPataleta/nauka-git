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
        // TODO: implement when API is available
    }

    fun showAttachments(dialogId: Long) {
        // TODO: implement when API is available
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
        // TODO: implement when API is available
    }

    fun selectMessages(dialogId: Long) {
        // TODO: implement when API is available
    }

    fun blockChat(dialogId: Long) {
        // TODO: implement when API is available
    }

    fun deleteChat(dialogId: Long) {
        // TODO: implement when API is available
    }
}
