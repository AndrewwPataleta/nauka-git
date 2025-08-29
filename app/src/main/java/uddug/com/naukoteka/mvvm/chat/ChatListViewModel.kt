package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
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

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedChats = MutableStateFlow<Set<Long>>(emptySet())
    val selectedChats: StateFlow<Set<Long>> = _selectedChats

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading

    private var loadChatsJob: kotlinx.coroutines.Job? = null

    fun loadFolders() {
        _uiState.value = ChatListUiState.Loading
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
        loadChatsJob?.cancel()
        val startTime = System.currentTimeMillis()
        currentFolderId = folderId
        loadChatsJob = viewModelScope.launch {
            try {
                val chats = chatRepository.getChats(folderId)
                val filteredChats = chats.filter { chat ->
                    chat.users.size <= 2
                }
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 500L) delay(500L - elapsed)
                _uiState.value = ChatListUiState.Success(filteredChats)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshChats() {
        loadChats(currentFolderId)
    }

    fun reorderFolders(fromIndex: Int, toIndex: Int) {
        _folders.update { current ->
            val mutable = current.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(if (toIndex > fromIndex) toIndex - 1 else toIndex, item)
            }
            mutable
        }
    }

    fun updateDialogNotifications(dialogId: Long, disabled: Boolean) {
        viewModelScope.launch {
            try {
                chatRepository.getDialogInfo(dialogId)
                _uiState.update { state ->
                    if (state is ChatListUiState.Success) {
                        val updatedChats = state.chats.map { chat ->
                            if (chat.dialogId == dialogId) {
                                chat.copy(notificationsDisable = disabled)
                            } else chat
                        }
                        ChatListUiState.Success(updatedChats)
                    } else state
                }
            } catch (_: Exception) {
            }
        }
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

    fun startSelection(dialogId: Long) {
        _isSelectionMode.value = true
        _selectedChats.value = setOf(dialogId)
    }

    fun toggleChatSelection(dialogId: Long) {
        _selectedChats.update { current ->
            val mutable = current.toMutableSet()
            if (!mutable.add(dialogId)) mutable.remove(dialogId)
            mutable
        }
    }

    fun clearSelection() {
        _selectedChats.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteSelectedChats() {
        val ids = _selectedChats.value
        viewModelScope.launch {
            ids.forEach { id ->
                try {
                    chatRepository.deleteDialog(id)
                } catch (_: Exception) {
                }
            }
            clearSelection()
            loadChats(currentFolderId)
        }
    }

    fun search(query: String) {
        if (query.length < 4) {
            _searchResults.value = emptyList()
            _isSearchLoading.value = false
            return
        }
        viewModelScope.launch {
            _isSearchLoading.value = true
            try {
                val dialogs = chatRepository.searchDialogs(query)
                    .filter { it.dialogType == 1 }
                val messages = chatRepository.searchMessages(query)
                    .mapNotNull { message ->
                        val info = runCatching {
                            chatRepository.getDialogInfo(message.dialogId)
                        }.getOrNull()
                        if (info?.users.orEmpty().size <= 2) message else null
                    }
                val results = dialogs.map { SearchResult.Dialog(it) } +
                        messages.map { SearchResult.Message(it) }
                _searchResults.value = results
            } catch (_: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearchLoading.value = false
            }
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

sealed class SearchResult {
    abstract val dialogId: Long
    data class Dialog(val data: SearchDialog) : SearchResult() {
        override val dialogId: Long get() = data.dialogId
    }
    data class Message(val data: SearchMessage) : SearchResult() {
        override val dialogId: Long get() = data.dialogId
    }
}