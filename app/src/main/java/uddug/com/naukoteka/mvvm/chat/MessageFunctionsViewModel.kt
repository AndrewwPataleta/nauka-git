package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

@HiltViewModel
class MessageFunctionsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    fun reply(messageId: Long) {
        // TODO: implement when API is available
    }

    fun forward(messageId: Long) {
        // TODO: implement when API is available
    }

    fun copy(messageId: Long) {
        // TODO: implement when API is available
    }

    fun select(messageId: Long) {
        // TODO: implement when API is available
    }

    fun showOriginal(messageId: Long) {
        // TODO: implement when API is available
    }

    fun delete(messageId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(messageId)
            } catch (e: Exception) {
                // handle error if needed
            }
        }
    }
}
