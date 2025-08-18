package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    private val _folders = MutableStateFlow<List<ChatFolder>>(emptyList())
    val folders: StateFlow<List<ChatFolder>> = _folders

    private var currentFolderId: Long? = null

    private val _events = MutableSharedFlow<ChatListEvents>()
    val events: SharedFlow<ChatListEvents> = _events.asSharedFlow()

    fun loadFolders() {
        viewModelScope.launch {
            try {
                val folderList = chatRepository.getFolders()
                _folders.value = folderList
                currentFolderId = folderList.firstOrNull()?.id
                loadChats(currentFolderId)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadChats(folderId: Long? = currentFolderId) {
        _uiState.value = ChatListUiState.Loading
        viewModelScope.launch {
            try {
                val chats = chatRepository.getChats(folderId)
                currentFolderId = folderId
                _uiState.value = ChatListUiState.Success(chats)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshChats() {
        loadChats(currentFolderId)
    }

    fun onFolderSelected(folderId: Long) {
        loadChats(folderId)
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