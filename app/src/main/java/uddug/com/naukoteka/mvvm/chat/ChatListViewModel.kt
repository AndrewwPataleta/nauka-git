package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
    userIdCache: UserIdCache,
    userUUIDCache: UserUUIDCache,
    private val socketService: SocketService,
) : ViewModel() {

    private val currentUserIds: Set<String> = listOfNotNull(
        userIdCache.entity?.takeIf { it.isNotBlank() },
        userUUIDCache.entity?.takeIf { it.isNotBlank() },
    ).toSet()

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState

    private val _folders = MutableStateFlow<List<ChatFolder>>(emptyList())
    val folders: StateFlow<List<ChatFolder>> = _folders

    private val _isFolderOrderChanged = MutableStateFlow(false)
    val isFolderOrderChanged: StateFlow<Boolean> = _isFolderOrderChanged

    private var lastSavedFolderOrder: List<Long> = emptyList()

    private val _currentFolderId = MutableStateFlow<Long?>(null)
    val currentFolderId: StateFlow<Long?> = _currentFolderId

    private val _events = MutableSharedFlow<ChatListEvents>()
    val events: SharedFlow<ChatListEvents> = _events.asSharedFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedChats = MutableStateFlow<Set<Long>>(emptySet())
    val selectedChats: StateFlow<Set<Long>> = _selectedChats

    private val _searchResults = MutableStateFlow(SearchResults())
    val searchResults: StateFlow<SearchResults> = _searchResults

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    private val _currentUser = MutableStateFlow<UserProfileFullInfo?>(null)
    val currentUser: StateFlow<UserProfileFullInfo?> = _currentUser

    private var loadChatsJob: kotlinx.coroutines.Job? = null
    private var searchJob: Job? = null

    init {
        socketService.connect()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.getProfileInfo().await()
                _currentUser.value = profile
            } catch (_: Exception) {
            }
        }
    }

    fun isMessageFromMe(ownerId: String?): Boolean {
        return ownerId.isNullOrBlank() || currentUserIds.contains(ownerId)
    }

    fun loadFolders() {
        _uiState.value = ChatListUiState.Loading
        viewModelScope.launch {
            try {
                val folderList = chatRepository.getFolders()
                applyFoldersUpdate(folderList)
                lastSavedFolderOrder = folderList.map { it.id }
                _isFolderOrderChanged.value = false
                val selectedId = _currentFolderId.value
                val initialFolderId = when {
                    selectedId != null && folderList.any { it.id == selectedId } -> selectedId
                    else -> folderList.firstOrNull()?.id
                }
                _currentFolderId.value = initialFolderId
                loadChats(initialFolderId)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        viewModelScope.launch {
            try {
                val updatedFolder = chatRepository.updateFolder(folderId, name = newName)
                _folders.update { current ->
                    current.map { if (it.id == folderId) updatedFolder else it }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun markFolderAsRead(folderId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.markFolderAsRead(folderId)
                _folders.update { current ->
                    current.map { if (it.id == folderId) it.copy(unreadCount = 0) else it }
                }
                if (_currentFolderId.value == folderId) {
                    _uiState.update { state ->
                        if (state is ChatListUiState.Success) {
                            val updatedChats = state.chats.map { chat ->
                                chat.copy(unreadMessages = 0, isUnread = false)
                            }
                            ChatListUiState.Success(updatedChats)
                        } else state
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            try {
                chatRepository.deleteFolder(folderId)
                loadFolders()
            } catch (_: Exception) {
            }
        }
    }

    fun loadChats(folderId: Long? = _currentFolderId.value) {
        _uiState.value = ChatListUiState.Loading
        loadChatsJob?.cancel()
        val startTime = System.currentTimeMillis()
        _currentFolderId.value = folderId
        loadChatsJob = viewModelScope.launch {
            try {
                val chats = chatRepository.getChats(folderId)
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed < 500L) delay(500L - elapsed)
                _uiState.value = ChatListUiState.Success(chats)
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refreshChats() {
        loadChats(_currentFolderId.value)
    }

    fun reorderFolders(fromIndex: Int, toIndex: Int) {
        _folders.update { current ->
            val mutable = current.toMutableList()
            if (fromIndex in mutable.indices && toIndex in mutable.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(if (toIndex > fromIndex) toIndex - 1 else toIndex, item)
            }
            _isFolderOrderChanged.value = mutable.map { it.id } != lastSavedFolderOrder
            mutable
        }
    }

    fun persistFolderOrder() {
        val currentOrder = _folders.value.map { it.id }
        if (currentOrder.isEmpty() || currentOrder == lastSavedFolderOrder) {
            _isFolderOrderChanged.value = false
            return
        }
        val selectedId = _currentFolderId.value
        viewModelScope.launch {
            try {
                val updatedFolders = chatRepository.reorderFolders(currentOrder)
                applyFoldersUpdate(updatedFolders)
                if (selectedId != null && updatedFolders.none { it.id == selectedId }) {
                    _currentFolderId.value = updatedFolders.firstOrNull()?.id
                } else {
                    _currentFolderId.value = selectedId
                }
                lastSavedFolderOrder = updatedFolders.map { it.id }
            } catch (_: Exception) {
                try {
                    val refreshedFolders = chatRepository.getFolders()
                    applyFoldersUpdate(refreshedFolders)
                    lastSavedFolderOrder = refreshedFolders.map { it.id }
                } catch (_: Exception) {
                }
            } finally {
                _isFolderOrderChanged.value = false
            }
        }
    }

    private fun applyFoldersUpdate(folders: List<ChatFolder>) {
        _folders.value = folders
        val selectedId = _currentFolderId.value
        if (selectedId != null && folders.none { it.id == selectedId }) {
            _currentFolderId.value = folders.firstOrNull()?.id
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

    fun updateDialogPin(dialogId: Long, pinned: Boolean) {
        viewModelScope.launch {
            try {
                chatRepository.getDialogInfo(dialogId)
                _uiState.update { state ->
                    if (state is ChatListUiState.Success) {
                        val updatedChats = state.chats.map { chat ->
                            if (chat.dialogId == dialogId) {
                                chat.copy(isPinned = pinned)
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
            loadChats(_currentFolderId.value)
        }
    }

    fun onSearchFocusChanged(isActive: Boolean) {
        _isSearchActive.value = isActive
        if (!isActive) {
            _searchResults.value = SearchResults()
            _isSearchLoading.value = false
            searchJob?.cancel()
        }
    }

    fun search(query: String) {
        if (query.length < SEARCH_MIN_QUERY_LENGTH) {
            searchJob?.cancel()
            _searchResults.value = SearchResults()
            _isSearchLoading.value = false
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearchLoading.value = true
            try {
                val dialogs = chatRepository.searchDialogs(query)
                    .map { SearchResult.Dialog(it) }
                val messages = chatRepository.searchMessages(query)
                    .map { SearchResult.Message(it) }
                _searchResults.value = SearchResults(
                    dialogs = dialogs,
                    messages = messages,
                )
            } catch (_: Exception) {
                _searchResults.value = SearchResults()
            } finally {
                _isSearchLoading.value = false
            }
        }
    }

    private companion object {
        const val SEARCH_MIN_QUERY_LENGTH = 1
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

data class SearchResults(
    val dialogs: List<SearchResult.Dialog> = emptyList(),
    val messages: List<SearchResult.Message> = emptyList(),
)

sealed class SearchResult {
    abstract val dialogId: Long
    data class Dialog(val data: SearchDialog) : SearchResult() {
        override val dialogId: Long get() = data.dialogId
    }
    data class Message(val data: SearchMessage) : SearchResult() {
        override val dialogId: Long get() = data.dialogId
    }
}