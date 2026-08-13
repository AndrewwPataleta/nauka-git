package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.gson.reflect.TypeToken
import uddug.com.data.cache.persistent.PersistentJsonCache
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.domain.entities.chat.ActiveCall
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
    userIdCache: UserIdCache,
    userUUIDCache: UserUUIDCache,
    private val socketService: SocketService,
    private val persistentCache: PersistentJsonCache,
) : ViewModel() {

    private fun dialogsCacheKey(folderId: Long?): String =
        "dialogs:${folderId ?: "all"}"

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

    // Активность по каналу `social` для строк списка: dialogId -> кто что делает.
    // Подписываемся на все загруженные диалоги, показываем «печатает…»/медиа в строке.
    private val _typingByDialog = MutableStateFlow<Map<Long, List<ChatActivity>>>(emptyMap())
    val typingByDialog: StateFlow<Map<Long, List<ChatActivity>>> = _typingByDialog
    // (dialogId, userId) -> локальное время последнего активного события (TTL).
    private val activitySeenAt = mutableMapOf<Pair<Long, String>, Long>()
    // (dialogId, userId) -> код действия #189.
    private val activityAction = mutableMapOf<Pair<Long, String>, Int>()
    // (dialogId, userId) -> серверный ts последнего 189:7 (debounce «стоп»).
    private val activityStoppedTs = mutableMapOf<Pair<Long, String>, Long>()
    // Диалоги, на которые сейчас подписаны, + участники для резолва имени.
    private var subscribedDialogIds: List<Long> = emptyList()
    private val dialogUsers = mutableMapOf<Long, List<User>>()
    private var socialResubJob: Job? = null

    private var loadChatsJob: kotlinx.coroutines.Job? = null
    private var searchJob: Job? = null

    init {
        socketService.connect()
        socketService.setOnEvent(SOCIAL_EVENT, SOCIAL_TAG) { data -> handleSocialEvent(data) }
        // Периодическая очистка истёкших статусов активности.
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (activitySeenAt.isNotEmpty()) recomputeActivity()
            }
        }
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val profile = withContext(Dispatchers.IO) { userProfileRepository.getProfileInfo().await() }
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
        loadChatsJob?.cancel()
        val startTime = System.currentTimeMillis()
        _currentFolderId.value = folderId
        val cacheKey = dialogsCacheKey(folderId)
        loadChatsJob = viewModelScope.launch {
            // Offline-first: если есть закэшированный список — показываем его сразу
            // (без спиннера), а свежий подтягиваем в фоне ниже. Cold miss — Loading.
            val cached = persistentCache.getList<Chat>(
                cacheKey,
                object : TypeToken<List<Chat>>() {}.type,
            )
            val showedCache = !cached.isNullOrEmpty()
            if (showedCache) {
                _uiState.value = ChatListUiState.Success(cached!!)
                updateSocialSubscription(cached)
                refreshActiveCalls(cached)
            } else {
                _uiState.value = ChatListUiState.Loading
            }
            try {
                val chats = chatRepository.getChats(folderId)
                if (!showedCache) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 500L) delay(500L - elapsed)
                }
                _uiState.value = ChatListUiState.Success(chats)
                persistentCache.putList(cacheKey, chats)
                updateSocialSubscription(chats)
                refreshActiveCalls(chats)
            } catch (e: Exception) {
                // Не затираем показанный из кэша контент ошибкой — он полезнее.
                if (!showedCache) {
                    _uiState.value = ChatListUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Для каждого открытого диалога запрашивает статус активного звонка
     * (GET dialogs/info?details=2) и дополняет уже показанный список чатов.
     * Запросы идут параллельно; ошибка по отдельному чату не ломает остальные.
     */
    private suspend fun refreshActiveCalls(chats: List<Chat>) {
        if (chats.isEmpty()) return
        val callsById: Map<Long, ActiveCall?> = coroutineScope {
            chats.map { chat ->
                async {
                    chat.dialogId to runCatching {
                        chatRepository.getDialogActiveCall(chat.dialogId)
                    }.getOrNull()
                }
            }.awaitAll().toMap()
        }
        _uiState.update { state ->
            if (state is ChatListUiState.Success) {
                ChatListUiState.Success(
                    state.chats.map { it.copy(activeCall = callsById[it.dialogId]) }
                )
            } else state
        }
    }

    // --- Активность чатов по каналу `social` (для строк списка) --------------

    /**
     * Подписывает список на статусы активности всех показанных диалогов
     * (subStats на пачку uref «312:<id>») и продлевает подписку каждые 45с,
     * т.к. серверная живёт ~1 мин. При смене набора диалогов переподписывается.
     */
    private fun updateSocialSubscription(chats: List<Chat>) {
        chats.forEach { dialogUsers[it.dialogId] = it.users }
        val ids = chats.map { it.dialogId }
        if (ids.toSet() == subscribedDialogIds.toSet()) return
        // Снимаем статусы диалогов, ушедших из списка.
        val gone = subscribedDialogIds.toSet() - ids.toSet()
        if (gone.isNotEmpty()) {
            activitySeenAt.keys.removeAll { it.first in gone }
            activityAction.keys.removeAll { it.first in gone }
            activityStoppedTs.keys.removeAll { it.first in gone }
        }
        subscribedDialogIds = ids
        emitSubStats(ids)
        socialResubJob?.cancel()
        socialResubJob = viewModelScope.launch {
            while (isActive) {
                delay(SUBSTATS_TTL_MS)
                if (subscribedDialogIds.isEmpty()) break
                emitSubStats(subscribedDialogIds)
            }
        }
        recomputeActivity()
    }

    private fun emitSubStats(ids: List<Long>) {
        if (ids.isEmpty()) return
        runCatching {
            socketService.sendMessage(
                SOCIAL_EVENT,
                mapOf("type" to "subStats", "rObjects" to ids.map { "$CHAT_UREF_TYPE:$it" }),
            )
        }
    }

    private fun handleSocialEvent(data: Any) {
        val json = runCatching { JSONObject(data.toString()) }.getOrNull() ?: return
        if (json.optString("type") != "evtChat") return
        val rObject = json.optString("rObject")
        if (!rObject.startsWith("$CHAT_UREF_TYPE:")) return
        val dialogId = rObject.substringAfter(':').toLongOrNull() ?: return
        if (dialogId !in subscribedDialogIds) return
        val payload = json.optJSONObject("payload") ?: return
        val user = payload.optString("user")
        if (user.isBlank() || user in currentUserIds) return
        val code = payload.optString("cSubType").substringAfter(':', "").toIntOrNull() ?: return
        val ts = json.optLong("ts", System.currentTimeMillis())
        val key = dialogId to user
        if (code == ACT_STOP) {
            activityStoppedTs[key] = ts
            activitySeenAt.remove(key)
            activityAction.remove(key)
            recomputeActivity()
            return
        }
        val stoppedTs = activityStoppedTs[key]
        if (stoppedTs != null && ts - stoppedTs in 0 until STOP_DEBOUNCE_MS) return
        activityStoppedTs.remove(key)
        activitySeenAt[key] = System.currentTimeMillis()
        activityAction[key] = code
        recomputeActivity()
    }

    private fun recomputeActivity() {
        val now = System.currentTimeMillis()
        val active = activitySeenAt.filterValues { now - it < TYPING_TTL_MS }.keys.toSet()
        activitySeenAt.keys.retainAll(active)
        activityAction.keys.retainAll(active)
        _typingByDialog.value = active
            .groupBy({ it.first }, { it.second })
            .mapValues { (dialogId, userIds) ->
                userIds.map { uid ->
                    val u = dialogUsers[dialogId]?.firstOrNull { it.userId == uid }
                        ?: User(userId = uid)
                    ChatActivity(u, activityAction[dialogId to uid] ?: ACT_TYPING)
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
                        }.sortedByDescending { it.isPinned }
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

    override fun onCleared() {
        socketService.removeEvent(SOCIAL_EVENT, SOCIAL_TAG)
        socialResubJob?.cancel()
        if (subscribedDialogIds.isNotEmpty()) {
            runCatching {
                socketService.sendMessage(
                    SOCIAL_EVENT,
                    mapOf(
                        "type" to "unsubStats",
                        "rObjects" to subscribedDialogIds.map { "$CHAT_UREF_TYPE:$it" },
                    ),
                )
            }
        }
        super.onCleared()
    }

    private companion object {
        const val SEARCH_MIN_QUERY_LENGTH = 1
        const val SOCIAL_EVENT = "social"
        const val SOCIAL_TAG = "ChatListSocial"
        const val CHAT_UREF_TYPE = "312"
        const val ACT_TYPING = 3
        const val ACT_STOP = 7
        const val SUBSTATS_TTL_MS = 45_000L
        const val TYPING_TTL_MS = 6_000L
        const val STOP_DEBOUNCE_MS = 1_000L
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