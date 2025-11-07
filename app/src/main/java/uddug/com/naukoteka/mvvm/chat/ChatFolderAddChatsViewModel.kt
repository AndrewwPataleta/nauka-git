package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

private const val SELECTED_IDS_KEY = "selected_ids"

@HiltViewModel
class ChatFolderAddChatsViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val chatStatusFormatter: ChatStatusFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initiallySelected: Set<Long> = savedStateHandle.get<LongArray>(SELECTED_IDS_KEY)
        ?.toSet().orEmpty()

    private val _uiState = MutableStateFlow<ChatFolderAddChatsUiState>(ChatFolderAddChatsUiState.Loading)
    val uiState: StateFlow<ChatFolderAddChatsUiState> = _uiState

    private val _events = MutableSharedFlow<ChatFolderAddChatsEvent>()
    val events: SharedFlow<ChatFolderAddChatsEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    private var allChats: List<Chat> = emptyList()
    private var allItems: List<ChatFolderSelectionItem> = emptyList()
    private var statusMap: Map<String, UserStatus> = emptyMap()

    init {
        loadChats()
    }

    fun onBackPressed() {
        viewModelScope.launch { _events.emit(ChatFolderAddChatsEvent.Cancel) }
    }

    fun onConfirmSelection() {
        val current = _uiState.value
        if (current is ChatFolderAddChatsUiState.Success) {
            val selectedItems = allItems.filter { current.selected.contains(it.dialogId) }
            viewModelScope.launch {
                _events.emit(ChatFolderAddChatsEvent.ChatsApplied(selectedItems))
            }
        }
    }

    fun onQueryChanged(query: String) {
        val current = _uiState.value
        if (current !is ChatFolderAddChatsUiState.Success) return
        _uiState.update { current.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.update { state ->
                    if (state is ChatFolderAddChatsUiState.Success) state.copy(searchResults = emptyList()) else state
                }
                return@launch
            }
            val results = mutableListOf<ChatFolderSelectionItem>()
            val users = runCatching { chatInteractor.searchUsers(query) }
                .getOrDefault(emptyList())
            results += users.mapNotNull { user -> mapUserToChat(user) }
            val groups = allItems.filter { it.isGroup && it.title.contains(query, ignoreCase = true) }
            results += groups
            val distinct = results.distinctBy { it.dialogId }
            _uiState.update { state ->
                if (state is ChatFolderAddChatsUiState.Success) state.copy(searchResults = distinct) else state
            }
        }
    }

    fun onChatClick(dialogId: Long) {
        val current = _uiState.value
        if (current is ChatFolderAddChatsUiState.Success) {
            val newSelected = current.selected.toMutableSet()
            if (!newSelected.add(dialogId)) {
                newSelected.remove(dialogId)
            }
            _uiState.update {
                current.copy(
                    selected = newSelected,
                    selectedItems = allItems.filter { newSelected.contains(it.dialogId) }
                )
            }
        }
    }

    private fun loadChats() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = ChatFolderAddChatsUiState.Loading
            try {
                val dialogs = chatInteractor.getDialogs()
                allChats = dialogs
                statusMap = loadStatuses(dialogs)
                allItems = dialogs.mapNotNull { chat -> mapChatToItem(chat, statusMap) }
                val selectedItems = allItems.filter { initiallySelected.contains(it.dialogId) }
                _uiState.value = ChatFolderAddChatsUiState.Success(
                    query = "",
                    chats = allItems,
                    searchResults = emptyList(),
                    selected = initiallySelected,
                    selectedItems = selectedItems
                )
            } catch (e: Exception) {
                _uiState.value = ChatFolderAddChatsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun loadStatuses(dialogs: List<Chat>): Map<String, UserStatus> {
        val userIds = dialogs
            .filter { it.dialogType == 1 }
            .mapNotNull { it.interlocutor.userId }
            .distinct()
        if (userIds.isEmpty()) return emptyMap()
        return runCatching { chatInteractor.getUsersStatus(userIds) }
            .getOrElse { emptyList() }
            .associateBy { it.userId }
    }

    private fun mapChatToItem(chat: Chat, statusMap: Map<String, UserStatus>): ChatFolderSelectionItem? {
        val isGroup = chat.dialogType != 1
        val title = if (isGroup) {
            chat.dialogName
        } else {
            chat.interlocutor.fullName ?: chat.interlocutor.nickname ?: chat.dialogName
        }
        val avatarUrl = if (isGroup) chat.dialogImage?.path else chat.interlocutor.image
        val initials = title
        val subtitle = if (isGroup) {
            chat.lastMessage.text
        } else {
            val userId = chat.interlocutor.userId
            formatStatus(statusMap[userId])
        }
        return ChatFolderSelectionItem(
            dialogId = chat.dialogId,
            title = title.orEmpty(),
            subtitle = subtitle,
            avatarUrl = avatarUrl,
            initials = initials,
            isGroup = isGroup
        )
    }

    private fun mapUserToChat(user: UserProfileFullInfo): ChatFolderSelectionItem? {
        val dialog = allChats.firstOrNull { chat ->
            chat.dialogType == 1 && chat.interlocutor.userId == user.id
        } ?: return null
        return mapChatToItem(dialog, statusMap)
    }

    private fun formatStatus(status: UserStatus?): String? {
        status ?: return null
        return if (status.isOnline) {
            chatStatusFormatter.online(ChatStatusTextMode.CONTACT)
        } else {
            status.lastSeen?.let { lastSeen ->
                runCatching { Instant.parse(lastSeen) }
                    .map { instant -> chatStatusFormatter.formatLastSeen(instant, ChatStatusTextMode.CONTACT) }
                    .getOrNull()
            }
        }
    }
}

sealed class ChatFolderAddChatsUiState {
    object Loading : ChatFolderAddChatsUiState()
    data class Success(
        val query: String,
        val chats: List<ChatFolderSelectionItem>,
        val searchResults: List<ChatFolderSelectionItem>,
        val selected: Set<Long>,
        val selectedItems: List<ChatFolderSelectionItem>,
    ) : ChatFolderAddChatsUiState()

    data class Error(val message: String) : ChatFolderAddChatsUiState()
}

sealed class ChatFolderAddChatsEvent {
    object Cancel : ChatFolderAddChatsEvent()
    data class ChatsApplied(val chats: List<ChatFolderSelectionItem>) : ChatFolderAddChatsEvent()
}
