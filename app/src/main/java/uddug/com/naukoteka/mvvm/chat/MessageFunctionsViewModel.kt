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
        
    }

    fun forward(messageId: Long) {
        
    }

    fun copy(messageId: Long) {
        
    }

    fun select(messageId: Long) {
        
    }

    fun showOriginal(messageId: Long) {
        
    }

    fun delete(messageId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessage(messageId)
            } catch (e: Exception) {
                
            }
        }
    }
}
