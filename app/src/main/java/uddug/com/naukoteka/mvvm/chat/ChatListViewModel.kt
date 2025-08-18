package uddug.com.naukoteka.mvvm.chat

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.domain.entities.profile.UserProfileFullInfo
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState


    private val _events = MutableSharedFlow<ChatListEvents>()
    val events: SharedFlow<ChatListEvents> = _events.asSharedFlow()

    @SuppressLint("CheckResult")
    fun loadChats() {
        _uiState.value = ChatListUiState.Loading
        viewModelScope.launch {

            try {
                viewModelScope.launch {
                    val chats = chatRepository.getChats()
                    println("chat count ${chats.size}")
                    _uiState.value = ChatListUiState.Success(chats)
                }

            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }

        }
    }

    fun refreshChats() {
        loadChats()
    }

    fun onChatClick(dialogId: Long) {
        viewModelScope.launch {
            _events.emit(ChatListEvents.OpenDialogDetail(dialogId))
        }
    }

    fun onClickCreateDialog() {
        viewModelScope.launch {
            _events.emit(ChatListEvents.OpenCreateDialog)
        }
    }
}

sealed class ChatListEvents {
    data class OpenDialogDetail(val dialogId: Long) : ChatListEvents()
    data object OpenCreateDialog : ChatListEvents()
}

sealed class ChatListUiState {
    object Loading : ChatListUiState()
    data class Success(
        val chats: List<uddug.com.domain.entities.chat.Chat>
    ) : ChatListUiState()

    data class Error(val message: String) : ChatListUiState()
}