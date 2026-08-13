package uddug.com.naukoteka.mvvm.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import uddug.com.domain.entities.chat.ActiveCall
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.ChatPoll
import uddug.com.domain.entities.chat.ChatPollOption
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.entities.chat.PollOption
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.call.CallRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.mvvm.chat.ContactInfo
import uddug.com.naukoteka.mvvm.chat.await
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
import java.time.Instant
import java.io.File
import java.io.EOFException
import java.util.Locale
import uddug.com.domain.entities.chat.File as ChatFile
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import javax.inject.Inject

private const val IMAGE_FILE_TYPE = 1
private const val VIDEO_FILE_TYPE = 30
private const val VOICE_FILE_TYPE = 21
private const val AUDIO_FILE_TYPE = 20
private const val DOCUMENT_FILE_TYPE = 100

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "mov", "avi", "wmv", "flv", "webm")
private val VOICE_EXTENSIONS = setOf("m4a", "aac", "amr", "3gp")
private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "ogg", "oga", "opus")
private val DOCUMENT_EXTENSIONS = setOf(
    "pdf",
    "doc",
    "docx",
    "xls",
    "xlsx",
    "ppt",
    "pptx",
    "txt",
    "zip",
    "rar",
    "7z",
    "rtf",
    "csv"
)

// cType, которые клиент имеет право отправлять: 1 - текст/медиа, 4 - голосовое,
// 7 - контакт, 9 - опрос. Системные типы (2, 3, 5, 6) создаёт только сервер;
// их отправка ломала валидацию на бэке (инцидент с сообщениями type=3).
private val ALLOWED_OUTGOING_CTYPES = setOf(1, 4, 7, 9)

@HiltViewModel
class ChatDialogViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService,
    private val chatStatusFormatter: ChatStatusFormatter,
    private val callRepository: CallRepository,
    private val chatDialogCache: ChatDialogCache,
    ) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDialogUiState>(ChatDialogUiState.Loading())
    val uiState: StateFlow<ChatDialogUiState> = _uiState

    private val _events = MutableSharedFlow<ChatDialogEvents>()
    val events: SharedFlow<ChatDialogEvents> = _events.asSharedFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedMessages = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMessages: StateFlow<Set<Long>> = _selectedMessages

    private var currentDialogID: Long? = null

    private var currentDialogInfo: DialogInfo? = null

    private var attachedFiles: MutableList<File> = mutableListOf()

    private var attachedContact: ContactInfo? = null

    private var selectedUser: UserProfileFullInfo? = null

    private var lastSentForwardAuthor: String? = null

    private var currentUser: UserProfileFullInfo? = null

    private val _currentDialogId = MutableStateFlow<Long?>(null)
    val currentDialogId: StateFlow<Long?> = _currentDialogId

    private val _isCurrentUserAdmin = MutableStateFlow(false)
    val isCurrentUserAdmin: StateFlow<Boolean> = _isCurrentUserAdmin

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId
    val currentUserName: String? get() = currentUser?.fullName

    // Участники текущего диалога — источник для @упоминаний (подсказки + разбор
    // тегов в сообщениях). Обновляется при загрузке инфо о диалоге.
    private val _participants = MutableStateFlow<List<User>>(emptyList())
    val participants: StateFlow<List<User>> = _participants

    // «Печатает…»: кто сейчас проявляет активность в этом диалоге. Данные идут по
    // ОТДЕЛЬНОМУ socket-каналу `social` (не `message`!): подписка subStats на uref
    // «312:<chatId>», приём evtChat с cSubType справочника #189 (3=пишет, 7=стоп).
    // См. wiki «Social».
    private val _typingUsers = MutableStateFlow<List<ChatActivity>>(emptyList())
    val typingUsers: StateFlow<List<ChatActivity>> = _typingUsers

    // userId -> локальное время последнего активного события (для TTL-очистки).
    private val typingSeenAt = mutableMapOf<String, Long>()
    // userId -> код действия из справочника #189 (3=пишет, 5=фото, 2=голос …).
    private val typingAction = mutableMapOf<String, Int>()
    // userId -> серверный ts последнего 189:7 (для debounce приоритета «стоп»).
    private val typingStoppedTs = mutableMapOf<String, Long>()
    // Троттлинг исходящих 189:3 (пинг раз в 3с) и авто-стоп по простою.
    private var lastTypingPingAt = 0L
    private var typingIdleStopJob: Job? = null
    // Подписка social на текущий чат + её продление (живёт ~1 мин на сервере).
    private var socialSubscribedDialogId: Long? = null
    private var socialResubJob: Job? = null

    private val _notificationsDisabled = MutableStateFlow(false)
    val notificationsDisabled: StateFlow<Boolean> = _notificationsDisabled

    // Pagination state. `isLoadingOlder` guards against concurrent requests
    // triggered by scroll-top. `hasMoreOlder` prevents further calls once the
    // server returns fewer than PAGE_SIZE items (end of history reached).
    private val _isLoadingOlder = MutableStateFlow(false)
    val isLoadingOlder: StateFlow<Boolean> = _isLoadingOlder
    private var hasMoreOlder: Boolean = true

    init {
        // Держим кэш диалога свежим: любое новое Success (входящие/отправленные
        // сообщения, правки) кладём в кэш, чтобы при следующем открытии показать
        // самое актуальное состояние мгновенно.
        viewModelScope.launch {
            _uiState.collect { st ->
                if (st is ChatDialogUiState.Success) {
                    currentDialogID?.let { chatDialogCache.put(it, st) }
                }
            }
        }
        socketService.connect()
        socketService.setOnEvent("message", LISTENER_TAG) { message ->
            handleIncomingMessage(message)
        }
        // Приём активности чата («печатает…» и медиа-статусы) по каналу `social`.
        socketService.setOnEvent(SOCIAL_EVENT, SOCIAL_TAG) { data ->
            handleSocialEvent(data)
        }
        // Истечение индикатора печати.
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (typingSeenAt.isNotEmpty()) recomputeTypingUsers()
            }
        }
    }

    // --- Исходящая активность (мы печатаем) ---------------------------------

    /**
     * Вызывается из поля ввода при печати. Пока пользователь печатает, шлём в
     * канал `social` активность «пишет сообщение» (189:3) не чаще раза в 3с
     * (TYPING_PING_MS). Если печать прекратилась, но сообщение не отправлено —
     * через TYPING_IDLE_STOP_MS шлём терминатор 189:7.
     */
    fun onUserTyping() {
        val dialogId = currentDialogID ?: return
        val now = System.currentTimeMillis()
        if (now - lastTypingPingAt >= TYPING_PING_MS) {
            lastTypingPingAt = now
            emitActivity(dialogId, ACT_TYPING)
        }
        typingIdleStopJob?.cancel()
        typingIdleStopJob = viewModelScope.launch {
            delay(TYPING_IDLE_STOP_MS)
            emitActivity(dialogId, ACT_STOP)
            lastTypingPingAt = 0L
        }
    }

    /**
     * Терминатор набора: вызывается при отправке сообщения (из emitChatMessage).
     * Шлём 189:7, только если до этого действительно печатали — иначе не спамим.
     */
    private fun emitTypingStop() {
        val dialogId = currentDialogID ?: return
        typingIdleStopJob?.cancel()
        typingIdleStopJob = null
        if (lastTypingPingAt == 0L) return
        lastTypingPingAt = 0L
        emitActivity(dialogId, ACT_STOP)
    }

    private fun emitActivity(dialogId: Long, code: Int) {
        emitSocial(
            mapOf(
                "type" to "evt",
                "rObjects" to listOf("$CHAT_UREF_TYPE:$dialogId"),
                "cSubType" to "$ACTIVITY_DICT:$code",
            )
        )
    }

    private fun emitSocial(payload: Map<String, Any>) {
        runCatching { socketService.sendMessage(SOCIAL_EVENT, payload) }
    }

    // --- Подписка на активность чата ----------------------------------------

    /**
     * Подписка на статусы чата по каналу `social`. Серверная подписка живёт ~1
     * минуту, поэтому продлеваем её каждые SUBSTATS_TTL_MS, пока чат открыт.
     */
    private fun subscribeSocial(dialogId: Long) {
        if (socialSubscribedDialogId == dialogId) return
        unsubscribeSocial()
        socialSubscribedDialogId = dialogId
        emitSocial(mapOf("type" to "subStats", "rObjects" to listOf("$CHAT_UREF_TYPE:$dialogId")))
        socialResubJob = viewModelScope.launch {
            while (isActive) {
                delay(SUBSTATS_TTL_MS)
                val id = socialSubscribedDialogId ?: break
                emitSocial(mapOf("type" to "subStats", "rObjects" to listOf("$CHAT_UREF_TYPE:$id")))
            }
        }
    }

    private fun unsubscribeSocial() {
        val id = socialSubscribedDialogId ?: return
        socialResubJob?.cancel()
        socialResubJob = null
        emitSocial(mapOf("type" to "unsubStats", "rObjects" to listOf("$CHAT_UREF_TYPE:$id")))
        socialSubscribedDialogId = null
        typingSeenAt.clear()
        typingStoppedTs.clear()
        typingAction.clear()
        _typingUsers.value = emptyList()
    }

    // --- Входящая активность (собеседник печатает) --------------------------

    private fun handleSocialEvent(data: Any) {
        val json = runCatching { JSONObject(data.toString()) }.getOrNull() ?: return
        when (json.optString("type")) {
            "evtChat" -> handleChatActivity(json)
            // evtChatPollStats — live-статистика опросов, отдельная фича ленты опросов.
        }
    }

    private fun handleChatActivity(json: JSONObject) {
        val dialogId = currentDialogID ?: return
        if (json.optString("rObject") != "$CHAT_UREF_TYPE:$dialogId") return
        val payload = json.optJSONObject("payload") ?: return
        val user = payload.optString("user")
        if (user.isBlank() || user == currentUser?.id) return
        // cSubType вида "189:3" — берём код из справочника #189.
        val code = payload.optString("cSubType").substringAfter(':', "").toIntOrNull() ?: return
        // ts генерит веб-сервер (мс), используем только для дедупа, не для показа.
        val ts = json.optLong("ts", System.currentTimeMillis())
        if (code == ACT_STOP) {
            typingStoppedTs[user] = ts
            typingSeenAt.remove(user)
            typingAction.remove(user)
            recomputeTypingUsers()
            return
        }
        // Debounce: 189:7 приоритетно; 189:3 в пределах <1с после стопа — отбрасываем.
        val stoppedTs = typingStoppedTs[user]
        if (stoppedTs != null && ts - stoppedTs in 0 until STOP_DEBOUNCE_MS) return
        typingStoppedTs.remove(user)
        // Запоминаем конкретное действие (пишет/фото/видео/файл/голос) — по нему
        // шапка выбирает иконку и текст статуса.
        typingSeenAt[user] = System.currentTimeMillis()
        typingAction[user] = code
        recomputeTypingUsers()
    }

    private fun recomputeTypingUsers() {
        val now = System.currentTimeMillis()
        val active = typingSeenAt.filterValues { now - it < TYPING_TTL_MS }.keys.toSet()
        typingSeenAt.keys.retainAll(active)
        typingAction.keys.retainAll(active)
        _typingUsers.value = active.map { id ->
            ChatActivity(resolveTypingUser(id), typingAction[id] ?: ACT_TYPING)
        }
    }

    private fun resolveTypingUser(userId: String): User {
        _participants.value.firstOrNull { it.userId == userId }?.let { return it }
        currentDialogInfo?.interlocutor?.takeIf { it.userId == userId }?.let { return it }
        return User(userId = userId)
    }


    fun loadMessages(dialogId: Long) {
        // Подписка на активность чата по каналу `social` (идемпотентна: повторный
        // вход в тот же диалог не переподписывается).
        subscribeSocial(dialogId)
        // Skip reload if we're already showing this dialog in Success state —
        // e.g. user returned from CreatePoll / ForwardMessage / AvatarView and
        // the ViewModel still has a fresh list. Reloading from scratch shows
        // the shimmer and takes a noticeable delay.
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success && currentDialogID == dialogId) {
            // Даже без перезагрузки списка — обновляем статус активного звонка,
            // чтобы после выхода из звонка баннер «К звонку» появлялся СРАЗУ, а не
            // через минуты (иначе рано-return пропускал refreshActiveCall).
            refreshActiveCall(dialogId)
            return
        }
        // Кэш: если этот диалог уже открывали в этой сессии (напр. вернулись из
        // звонка) — сразу показываем прошлые сообщения без спиннера, а свежие
        // подтянем в фоне ниже. Иначе — обычный Loading.
        val cached = chatDialogCache.get(dialogId)
        var showedCache = cached != null
        if (cached != null) {
            currentDialogID = dialogId
            _currentDialogId.value = dialogId
            _uiState.value = cached
        } else {
            _uiState.value = ChatDialogUiState.Loading()
        }
        hasMoreOlder = true
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                // Холодный старт: in-memory кэша нет, но список сообщений мог
                // сохраниться в persistent-кэше (Room). Показываем его сразу, до
                // сетевого запроса, чтобы диалог не открывался пустым спиннером.
                if (!showedCache) {
                    val persisted = chatDialogCache.getPersisted(dialogId)
                    if (persisted != null) {
                        currentDialogID = dialogId
                        _currentDialogId.value = dialogId
                        _uiState.value = persisted
                        showedCache = true
                    }
                }
                val user = withContext(Dispatchers.IO) { userRepository.getProfileInfo().await() }
                currentUser = user
                _currentUserId.value = user.id

                val info = chatInteractor.getDialogInfo(dialogId)
                currentDialogInfo = info
                _participants.value = info.users.orEmpty()
                currentDialogID = dialogId
                _currentDialogId.value = dialogId

                val name: String = when {
                    info.interlocutor != null -> info.interlocutor?.fullName.orEmpty()
                    else -> info.name.orEmpty()
                }
                val image: String = when {
                    info.interlocutor != null -> info.interlocutor?.image.orEmpty()
                    else -> info.dialogImage?.path.orEmpty()
                }
                val firstParticipantName = info.users?.firstOrNull()?.fullName.orEmpty()
                var status: String? = null
                val isGroup = (info.users?.size ?: 0) > 2
                val isAdmin = computeIsCurrentUserAdmin(info, user.id)
                _isCurrentUserAdmin.value = isAdmin
                if (!isGroup) {
                    val userId = info.interlocutor?.userId
                    if (userId != null) {
                        try {
                            val userStatus = chatInteractor.getUsersStatus(listOf(userId)).firstOrNull()
                            status = if (userStatus?.isOnline == true) {
                                chatStatusFormatter.online()
                            } else {
                                userStatus?.lastSeen?.let { formatLastSeen(it) }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                // Не затираем показанный из кэша контент промежуточным Loading.
                if (!showedCache) {
                    _uiState.value = ChatDialogUiState.Loading(
                        chatName = name,
                        chatImage = image,
                        isGroup = isGroup,
                        firstParticipantName = firstParticipantName,
                        status = status
                    )
                }

                val currentUserId = user.id ?: return@launch
                val messages = chatInteractor.getMessagesWithOwnerInfo(
                    currentUserId = currentUserId,
                    dialogId = dialogId,
                    limit = PAGE_SIZE,
                    lastMessageId = null,
                ).sortedBy { it.createdAt }
                hasMoreOlder = messages.size >= PAGE_SIZE
                // Артиф. задержку против мигания спиннера делаем только когда
                // реально показывали спиннер (нет кэша).
                if (!showedCache) {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 500L) delay(500L - elapsed)
                }
                val success = ChatDialogUiState.Success(
                    chats = messages,
                    chatName = name,
                    chatImage = image,
                    isGroup = isGroup,
                    firstParticipantName = firstParticipantName,
                    status = status,
                    attachedFiles = attachedFiles.toList(),
                    pinnedMessages = info.pinnedMessages,
                )
                _uiState.value = success
                chatDialogCache.put(dialogId, success)
                applyPendingForwardIfAny()
                markMessagesRead(dialogId, messages)
                refreshActiveCall(dialogId)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ChatDialogUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches whether the dialog currently has an active call (plus the live
     * participant count) and reflects it in the "ongoing call" banner. cType
     * 2/3/6 socket signals keep it fresh afterwards — see
     * [updateActiveCallFromSignal].
     */
    private fun refreshActiveCall(dialogId: Long) {
        viewModelScope.launch {
            val activeCall = runCatching {
                chatInteractor.getDialogActiveCall(dialogId)
            }.getOrNull()
            val activeParticipants = if (activeCall != null) {
                runCatching {
                    callRepository.getParticipants(dialogId)
                        .filter { it.status == CALL_STATUS_PARTICIPATING }
                }.getOrDefault(emptyList())
            } else {
                emptyList()
            }
            val participantsCount = activeParticipants.size
            // Участники, реально в звонке — для кластера аватарок в баннере
            // «К звонку». Берём и имя (для инициалов, если нет фото), и картинку —
            // иначе у тест-юзеров без аватарок кластер был пуст и показывался
            // аватар группы.
            val members = activeParticipants.map { p ->
                CallMemberPreview(
                    name = p.fullName?.takeIf { it.isNotBlank() },
                    imageUrl = p.imageUrl?.takeIf { it.isNotBlank() },
                )
            }
            val current = _uiState.value
            if (current is ChatDialogUiState.Success) {
                _uiState.value = current.copy(
                    activeCall = activeCall,
                    activeCallParticipantsCount = participantsCount,
                    activeCallMembers = members,
                )
            }
        }
    }

    private fun updateActiveCallFromSignal(started: Boolean, cType: Int?) {
        val current = _uiState.value as? ChatDialogUiState.Success ?: return
        if (started) {
            // Show the banner at once with the call type from the signal;
            // refreshActiveCall() then fills in the live participant count.
            _uiState.value = current.copy(
                activeCall = ActiveCall(id = 0L, format = 0, type = cType ?: 2),
            )
            currentDialogID?.let { refreshActiveCall(it) }
        } else {
            _uiState.value = current.copy(
                activeCall = null,
                activeCallParticipantsCount = 0,
            )
        }
    }

    fun loadMessagesByPeer(interlocutorId: String) {
        // See rationale in [loadMessages] — avoid reloading when returning
        // from nested fragments if the chat is already displayed.
        if (_uiState.value is ChatDialogUiState.Success) {
            return
        }
        _uiState.value = ChatDialogUiState.Loading()
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                val user = withContext(Dispatchers.IO) { userRepository.getProfileInfo().await() }
                currentUser = user
                _currentUserId.value = user.id

                val info = chatInteractor.getDialogInfoByPeer(interlocutorId)
                currentDialogInfo = info
                _participants.value = info.users.orEmpty()
                val dialogId = info.id
                _currentDialogId.value = dialogId

                val name = info.interlocutor?.fullName.orEmpty()
                val isGroup = (info.users?.size ?: 0) > 2
                val image = if (isGroup) {
                    info.dialogImage?.path.orEmpty()
                } else {
                    info.interlocutor?.image.orEmpty()
                }
                val firstParticipantName = info.users?.firstOrNull()?.fullName.orEmpty()
                var status: String? = null
                val isAdmin = computeIsCurrentUserAdmin(info, user.id)
                _isCurrentUserAdmin.value = isAdmin
                if (!isGroup) {
                    val userId = info.interlocutor?.userId
                    if (userId != null) {
                        try {
                            val userStatus = chatInteractor.getUsersStatus(listOf(userId)).firstOrNull()
                            status = if (userStatus?.isOnline == true) {
                                chatStatusFormatter.online()
                            } else {
                                userStatus?.lastSeen?.let { formatLastSeen(it) }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                _uiState.value = ChatDialogUiState.Loading(
                    chatName = name,
                    chatImage = image,
                    isGroup = isGroup,
                    firstParticipantName = firstParticipantName,
                    status = status
                )

                if (dialogId != 0L) {
                    currentDialogID = dialogId
                    val currentUserId = user.id ?: return@launch
                    val messages = try {
                        chatInteractor.getMessagesWithOwnerInfo(
                            currentUserId = currentUserId,
                            dialogId = dialogId,
                            limit = PAGE_SIZE,
                            lastMessageId = null,
                        ).sortedBy { it.createdAt }
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 403) {
                            _currentDialogId.value = 0L
                            currentDialogID = 0L
                            currentDialogInfo = info.copy(id = 0L)
                            null
                        } else throw e
                    }
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 500L) delay(500L - elapsed)
                    _uiState.value = ChatDialogUiState.Success(
                        chats = messages ?: emptyList(),
                        chatName = name,
                        chatImage = image,
                        isGroup = isGroup,
                        firstParticipantName = firstParticipantName,
                        status = status,
                        attachedFiles = attachedFiles.toList(),
                        pinnedMessages = info.pinnedMessages,
                    )
                    if (messages != null) markMessagesRead(dialogId, messages)
                } else {
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 500L) delay(500L - elapsed)
                    _uiState.value = ChatDialogUiState.Success(
                        chats = emptyList(),
                        chatName = name,
                        chatImage = image,
                        isGroup = false,
                        firstParticipantName = firstParticipantName,
                        status = status,
                        attachedFiles = attachedFiles.toList(),
                        pinnedMessages = info.pinnedMessages,
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ChatDialogUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Loads the previous page of messages (older than currently shown). Called
     * when the user scrolls near the top of the chat. Silently no-ops if
     * there's nothing more to load or a request is already in flight.
     */
    fun loadOlderMessages() {
        if (!hasMoreOlder) return
        if (_isLoadingOlder.value) return
        val dialogId = currentDialogID ?: return
        val state = _uiState.value as? ChatDialogUiState.Success ?: return
        val currentUserId = currentUser?.id ?: return
        val oldestMessageId = state.chats.minByOrNull { it.createdAt }?.id ?: return

        _isLoadingOlder.value = true
        viewModelScope.launch {
            try {
                val older = chatInteractor.getMessagesWithOwnerInfo(
                    currentUserId = currentUserId,
                    dialogId = dialogId,
                    limit = PAGE_SIZE,
                    lastMessageId = oldestMessageId,
                ).sortedBy { it.createdAt }

                if (older.size < PAGE_SIZE) {
                    hasMoreOlder = false
                }
                if (older.isNotEmpty()) {
                    val existingIds = state.chats.map { it.id }.toHashSet()
                    val merged = (older.filter { it.id !in existingIds } + state.chats)
                        .sortedBy { it.createdAt }
                    val latest = _uiState.value
                    if (latest is ChatDialogUiState.Success) {
                        _uiState.value = latest.copy(chats = merged)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingOlder.value = false
            }
        }
    }

    fun refreshDialogInfo() {
        val dialogId = currentDialogID ?: return
        viewModelScope.launch {
            try {
                val info = chatInteractor.getDialogInfo(dialogId)
                currentDialogInfo = info
                _participants.value = info.users.orEmpty()
                _currentDialogId.value = dialogId
                _isCurrentUserAdmin.value = computeIsCurrentUserAdmin(info, currentUser?.id)
                val isGroup = (info.users?.size ?: 0) > 2
                val name: String = when {
                    info.interlocutor != null -> info.interlocutor?.fullName.orEmpty()
                    else -> info.name.orEmpty()
                }
                val image: String = when {
                    info.interlocutor != null -> info.interlocutor?.image.orEmpty()
                    else -> info.dialogImage?.path.orEmpty()
                }
                val firstParticipantName = info.users?.firstOrNull()?.fullName.orEmpty()
                when (val currentState = _uiState.value) {
                    is ChatDialogUiState.Success -> {
                        _uiState.value = currentState.copy(
                            chatName = name,
                            chatImage = image,
                            isGroup = isGroup,
                            firstParticipantName = firstParticipantName
                        )
                    }
                    is ChatDialogUiState.Loading -> {
                        _uiState.value = currentState.copy(
                            chatName = name,
                            chatImage = image,
                            isGroup = isGroup,
                            firstParticipantName = firstParticipantName
                        )
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
            }
        }
    }

    fun updateNotificationsDisabled(disabled: Boolean) {
        _notificationsDisabled.value = disabled
    }

    fun updateCurrentMessage(newMessage: String) {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(currentMessage = newMessage)
        }
    }

    /**
     * Тап по @упоминанию: тянем полный профиль пользователя по id
     * (GET core/user_profile/:userId) и просим экран показать его.
     */
    fun openUserProfile(userId: String) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { userRepository.getProfileInfo(userId).await() }
            }.onSuccess { profile ->
                _events.emit(ChatDialogEvents.OpenUserProfile(profile))
            }.onFailure {
                Log.e("ChatViewModel", "openUserProfile failed: ${it.message}")
            }
        }
    }

    fun onChatDetailClick() {
        println("try emit detail ${currentDialogID}")
        currentDialogInfo?.let { info ->
            viewModelScope.launch {
                _events.emit(
                    ChatDialogEvents.OpenChatProfileDetail(
                        dialogId = currentDialogID ?: 0,
                        dialogInfo = info
                    )
                )
            }
        }

    }

    fun attachFiles(files: List<File>) {
        attachedFiles.addAll(files)
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(attachedFiles = attachedFiles.toList())
        }
    }

    fun clearAttachedFiles() {
        attachedFiles.clear()
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(attachedFiles = emptyList())
        }
    }

    fun removeAttachedFile(file: File) {
        attachedFiles.remove(file)
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(attachedFiles = attachedFiles.toList())
        }
    }

    fun attachContact(contact: ContactInfo) {
        attachedContact = contact
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(attachedContact = contact)
        }
    }

    fun attachUserContact(user: UserProfileFullInfo) {
        selectedUser = user
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(selectedContact = user)
        }
    }

    fun clearSelectedContact() {
        selectedUser = null
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(selectedContact = null)
        }
    }

    fun clearAttachedContact() {
        attachedContact = null
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(attachedContact = null)
        }
    }

    fun setReplyMessage(message: MessageChat) {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(
                replyMessage = message,
                editingMessage = null,
                currentMessage = if (currentState.editingMessage != null) "" else currentState.currentMessage,
            )
        }
    }

    fun clearReplyMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(replyMessage = null)
        }
    }

    fun setPendingForward(messageIds: List<Long>, text: String?, authorName: String?) {
        val forward = PendingForward(messageIds = messageIds, text = text, authorName = authorName)
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(
                pendingForward = forward,
                replyMessage = null,
                editingMessage = null,
                currentMessage = if (currentState.editingMessage != null) "" else currentState.currentMessage,
            )
        } else {
            pendingForwardFromArgs = forward
        }
    }

    private var pendingForwardFromArgs: PendingForward? = null

    fun applyPendingForwardIfAny() {
        val forward = pendingForwardFromArgs ?: return
        pendingForwardFromArgs = null
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(
                pendingForward = forward,
                replyMessage = null,
                editingMessage = null,
            )
        }
    }

    fun clearForwardMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(pendingForward = null)
        }
    }

    fun startEditingMessage(message: MessageChat) {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success && message.isMine) {
            attachedFiles.clear()
            attachedContact = null
            selectedUser = null
            _uiState.value = currentState.copy(
                editingMessage = message,
                currentMessage = message.text.orEmpty(),
                attachedFiles = emptyList(),
                replyMessage = null,
                attachedContact = null,
                selectedContact = null,
            )
        }
    }

    fun clearEditingMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(
                editingMessage = null,
                currentMessage = ""
            )
        }
    }

    fun startSelection(messageId: Long) {
        _isSelectionMode.value = true
        _selectedMessages.value = setOf(messageId)
    }

    fun toggleMessageSelection(messageId: Long) {
        _selectedMessages.update { current ->
            val mutable = current.toMutableSet()
            if (!mutable.add(messageId)) mutable.remove(messageId)
            mutable
        }
    }

    fun clearSelection() {
        _selectedMessages.value = emptySet()
        _isSelectionMode.value = false
    }

    fun deleteSelectedMessages() {
        val ids = _selectedMessages.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            try {
                chatInteractor.deleteMessages(ids.toList())
            } catch (_: Exception) {
                
            } finally {
                clearSelection()
            }
        }
    }

    private fun determineFileType(file: File): Int {
        val extension = file.extension.lowercase()
        return when {
            extension in IMAGE_EXTENSIONS -> IMAGE_FILE_TYPE
            extension in VIDEO_EXTENSIONS -> VIDEO_FILE_TYPE
            extension in VOICE_EXTENSIONS -> VOICE_FILE_TYPE
            extension in AUDIO_EXTENSIONS -> AUDIO_FILE_TYPE
            extension in DOCUMENT_EXTENSIONS -> DOCUMENT_FILE_TYPE
            extension.isBlank() -> IMAGE_FILE_TYPE
            else -> DOCUMENT_FILE_TYPE
        }
    }

    private fun ChatFile.toFileDescriptor(): FileDescriptor {
        return FileDescriptor(
            id = id,
            fileType = resolveExistingFileType(),
        )
    }

    private fun ChatFile.resolveExistingFileType(): Int {
        fileType?.let { return it }

        val contentType = contentType?.lowercase(Locale.ROOT)
        val extension = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase(Locale.ROOT)

        return when {
            extension != null && extension in VOICE_EXTENSIONS -> VOICE_FILE_TYPE
            extension != null && extension in AUDIO_EXTENSIONS -> AUDIO_FILE_TYPE
            extension != null && extension in VIDEO_EXTENSIONS -> VIDEO_FILE_TYPE
            extension != null && extension in IMAGE_EXTENSIONS -> IMAGE_FILE_TYPE
            contentType?.startsWith("audio/") == true -> AUDIO_FILE_TYPE
            contentType?.startsWith("video/") == true -> VIDEO_FILE_TYPE
            contentType?.startsWith("image/") == true -> IMAGE_FILE_TYPE
            else -> DOCUMENT_FILE_TYPE
        }
    }

    private fun buildUserContactPayload(user: UserProfileFullInfo): String? {
        val json = JSONObject()
        user.id?.let { json.put("id", it) }
        user.fullName?.let { json.put("fullName", it) }
        user.nickname?.let { json.put("nickname", it) }
        user.phone?.let { json.put("phone", it) }
        user.image?.path?.let { json.put("image", it) }
        return if (json.length() == 0) null else json.toString()
    }

    private fun buildPhoneContactPayload(contact: ContactInfo): String {
        return JSONObject().apply {
            put("name", contact.name)
            put("phone", contact.phone)
        }.toString()
    }

    private fun createContactSocketMessage(
        dialog: DialogInfo,
        payload: String,
        replyId: Long?,
    ): ChatSocketMessage? {
        return if (dialog.id != 0L) {
            ChatSocketMessage(
                dialog = dialog.id,
                cType = 7,
                text = payload,
                owner = currentUser?.id.orEmpty(),
                answered = replyId
            )
        } else {
            val peer = dialog.interlocutor?.userId ?: return null
            ChatSocketMessage(
                interlocutor = peer,
                cType = 7,
                text = payload,
                owner = currentUser?.id.orEmpty(),
                answered = replyId
            )
        }
    }

    /**
     * Единая точка отправки сообщений в сокет. Гарантирует, что клиент никогда
     * не отправит системный тип (2, 3, 5, 6) и не пришлёт поля, которые заполняет
     * только сервер в ответных сообщениях (id/read/createdAt/ownerName/
     * ownerAvatarUrl). Иначе такие поля могли перетереть type/read на бэке.
     */
    private fun emitChatMessage(message: ChatSocketMessage) {
        if (message.cType !in ALLOWED_OUTGOING_CTYPES) {
            Log.e(
                "ChatViewModel",
                "Refusing to emit message with system/unknown cType=${message.cType}; " +
                    "client must never send system message types"
            )
            return
        }
        val sanitized = message.copy(
            id = null,
            read = null,
            createdAt = null,
            ownerName = null,
            ownerAvatarUrl = null,
        )
        socketService.sendMessage("message", sanitized)
        // Отправили сообщение → завершаем индикатор набора терминатором 189:7.
        emitTypingStop()
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val currentState = _uiState.value
            val successState = currentState as? ChatDialogUiState.Success
            val replyId = successState?.replyMessage?.id
            val pendingForward = successState?.pendingForward
            val forwardId = pendingForward?.messageIds?.firstOrNull()
            val forwardIds = pendingForward?.messageIds
            val sanitizedText = text.trim()
            val outgoingText = if (forwardId != null) null else sanitizedText.takeIf { it.isNotEmpty() }

            selectedUser?.let { user ->
                val payload = buildUserContactPayload(user) ?: return@launch
                val message = createContactSocketMessage(dialog, payload, replyId) ?: return@launch
                emitChatMessage(message)
                selectedUser = null
                if (currentState is ChatDialogUiState.Success) {
                    _uiState.value = currentState.copy(
                        currentMessage = "",
                        selectedContact = null
                    )
                }
                clearReplyMessage()
                return@launch
            }

            attachedContact?.let { contact ->
                val payload = buildPhoneContactPayload(contact)
                val message = createContactSocketMessage(dialog, payload, replyId) ?: return@launch
                emitChatMessage(message)
                attachedContact = null
                if (currentState is ChatDialogUiState.Success) {
                    _uiState.value = currentState.copy(
                        currentMessage = "",
                        attachedContact = null
                    )
                }
                clearReplyMessage()
                return@launch
            }

            if (sanitizedText.isEmpty() && attachedFiles.isEmpty() && forwardId == null) {
                Log.d("ChatViewModel", "Message is blank and no files attached — skipping")
                return@launch
            }
            val editingMessage = successState?.editingMessage

            if (editingMessage != null) {
                val updatedMessage = try {
                    chatInteractor.updateMessage(
                        dialogId = dialog.id,
                        messageId = editingMessage.id,
                        text = sanitizedText,
                        files = editingMessage.files.mapNotNull { it.toFileDescriptor() },
                    )
                } catch (e: EOFException) {
                    Log.w(
                        "ChatViewModel",
                        "Empty response received when updating message, using local data",
                        e
                    )
                    editingMessage.copy(text = sanitizedText)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Failed to update message", e)
                    return@launch
                }

                val updatedChats = successState.chats.map { existing ->
                    if (existing.id == updatedMessage.id) {
                        existing.copy(
                            text = updatedMessage.text,
                            type = updatedMessage.type,
                            files = updatedMessage.files,
                            readCount = updatedMessage.readCount,
                            createdAt = updatedMessage.createdAt,
                            ownerId = updatedMessage.ownerId,
                            isMine = updatedMessage.isMine
                        )
                    } else {
                        existing
                    }
                }
                _uiState.value = successState.copy(
                    chats = updatedChats,
                    currentMessage = "",
                    editingMessage = null
                )
                return@launch
            }

            val uploadRequiresRaw = attachedFiles.any { determineFileType(it) != IMAGE_FILE_TYPE }
            val uploaded = if (attachedFiles.isNotEmpty()) {
                try {
                    chatInteractor.uploadFiles(attachedFiles, uploadRequiresRaw)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Failed to upload attachments", e)
                    emptyList()
                }
            } else emptyList()

            if (attachedFiles.isNotEmpty() && uploaded.size != attachedFiles.size) {
                Log.e(
                    "ChatViewModel",
                    "Attachment upload failed: expected ${attachedFiles.size} files, got ${uploaded.size}"
                )
                return@launch
            }

            val fileDescriptors = uploaded.mapIndexed { index, uploadedFile ->
                val type = attachedFiles.getOrNull(index)?.let { determineFileType(it) } ?: DOCUMENT_FILE_TYPE
                FileDescriptor(
                    id = uploadedFile.id,
                    fileType = type
                )
            }

            val cType = 1

            val useForwardedN = forwardIds != null && forwardIds.size > 1
            val message = if (dialog.id != 0L) {
                ChatSocketMessage(
                    dialog = dialog.id,
                    cType = cType,
                    text = outgoingText,
                    owner = currentUser?.id.orEmpty(),
                    files = fileDescriptors.ifEmpty { null },
                    answered = replyId,
                    forwarded = if (!useForwardedN) forwardId else null,
                    forwardedn = if (useForwardedN) forwardIds else null
                )
            } else {
                val peer = dialog.interlocutor?.userId ?: return@launch
                ChatSocketMessage(
                    interlocutor = peer,
                    cType = cType,
                    text = outgoingText,
                    owner = currentUser?.id.orEmpty(),
                    files = fileDescriptors.ifEmpty { null },
                    answered = replyId,
                    forwarded = if (!useForwardedN) forwardId else null,
                    forwardedn = if (useForwardedN) forwardIds else null
                )
            }
            if (successState != null) {
                _uiState.value = successState.copy(currentMessage = "")
            }
            lastSentForwardAuthor = pendingForward?.authorName
            Log.d("ChatViewModel", "Sending socket message: $message")
            emitChatMessage(message)
            clearAttachedFiles()
            clearReplyMessage()
            clearForwardMessage()
        }
    }

    fun sendPoll(pollId: String) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val ownerId = currentUser?.id.orEmpty()

            val message = if (dialog.id != 0L) {
                ChatSocketMessage(
                    dialog = dialog.id,
                    cType = 9,
                    owner = ownerId,
                    pollId = pollId
                )
            } else {
                val peer = dialog.interlocutor?.userId ?: return@launch
                ChatSocketMessage(
                    interlocutor = peer,
                    cType = 9,
                    owner = ownerId,
                    pollId = pollId
                )
            }

            emitChatMessage(message)
        }
    }

    fun voteInPoll(pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
            try {
                val statsPreview = chatInteractor.answerPoll(pollId, optionIds)
                val selectedSet = optionIds.toSet()
                val currentState = _uiState.value
                if (currentState is ChatDialogUiState.Success) {
                    val updatedChats = currentState.chats.map { message ->
                        val poll = message.poll
                        if (poll == null || poll.id != pollId) {
                            message
                        } else if (statsPreview != null) {
                            val statsOptions = statsPreview.options.associateBy { it.id }
                            val answeredIds = statsPreview.options
                                .filter { it.isVoted }
                                .map { it.id }
                                .toSet()
                            val updatedOptions = poll.options.map { option ->
                                val stat = statsOptions[option.id]
                                option.copy(
                                    isVoted = option.id in answeredIds,
                                    percent = stat?.percent ?: option.percent,
                                    voteCount = stat?.voteCount.takeIf { it != 0 } ?: option.voteCount,
                                    isRightAnswer = stat?.isRightAnswer ?: option.isRightAnswer,
                                    description = stat?.description ?: option.description,
                                )
                            }
                            message.copy(poll = poll.copy(options = updatedOptions))
                        } else {
                            val updatedOptions = poll.options.map { option ->
                                option.copy(isVoted = option.id in selectedSet)
                            }
                            message.copy(poll = poll.copy(options = updatedOptions))
                        }
                    }
                    _uiState.value = currentState.copy(chats = updatedChats)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to vote in poll", e)
            }
        }
    }

    fun stopPoll(pollId: String) {
        viewModelScope.launch {
            try {
                chatInteractor.stopPoll(pollId)
                // Mark the local poll as stopped without re-fetching — GET
                // /dialogs/poll/:id returns a minimal payload (no options) so
                // we preserve what we already have from the message feed.
                val currentState = _uiState.value
                if (currentState is ChatDialogUiState.Success) {
                    val updatedChats = currentState.chats.map { message ->
                        val poll = message.poll
                        if (poll != null && poll.id == pollId) {
                            message.copy(poll = poll.copy(isStopped = true))
                        } else {
                            message
                        }
                    }
                    _uiState.value = currentState.copy(chats = updatedChats)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to stop poll", e)
            }
        }
    }

    fun sendVoiceMessage(file: File) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: run {
                Log.e("ChatViewModel", "Voice: no currentDialogInfo")
                return@launch
            }
            Log.d("ChatViewModel", "Voice: uploading ${file.name} (${file.length()} bytes)")
            val uploaded = try {
                val requiresRawUpload = determineFileType(file) != IMAGE_FILE_TYPE
                chatInteractor.uploadFiles(listOf(file), requiresRawUpload)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Voice: upload exception", e)
                emptyList()
            }
            Log.d("ChatViewModel", "Voice: uploaded ${uploaded.size} files: ${uploaded.map { it.id }}")
            val descriptor = uploaded.firstOrNull()?.let { uploadedFile ->
                FileDescriptor(
                    id = uploadedFile.id,
                    fileType = determineFileType(file)
                )
            }
            if (descriptor == null) {
                Log.e("ChatViewModel", "Voice message upload failed — message not sent")
                return@launch
            }
            val message = if (dialog.id != 0L) {
                ChatSocketMessage(
                    dialog = dialog.id,
                    cType = 4,
                    owner = currentUser?.id.orEmpty(),
                    files = listOf(descriptor)
                )
            } else {
                val peer = dialog.interlocutor?.userId ?: return@launch
                ChatSocketMessage(
                    interlocutor = peer,
                    cType = 4,
                    owner = currentUser?.id.orEmpty(),
                    files = listOf(descriptor)
                )
            }
            Log.d("ChatViewModel", "Voice: sending socket message cType=4, fileId=${descriptor.id}")
            emitChatMessage(message)
            Log.d("ChatViewModel", "Voice: socket message sent successfully")
        }
    }

    private fun markMessagesRead(dialogId: Long, messages: List<MessageChat>) {
        val messageIds = messages
            .filter { !it.isMine && (it.readCount ?: 0) < READ_STATUS }
            .map { it.id }
        if (messageIds.isEmpty()) return
        viewModelScope.launch {
            try {
                chatInteractor.markMessagesRead(dialogId, messageIds, READ_STATUS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatLastSeen(lastSeen: String): String {
        return runCatching { Instant.parse(lastSeen) }
            .map { chatStatusFormatter.formatLastSeen(it, GENERIC) }
            .getOrDefault("")
    }

    private fun handleIncomingMessage(message: Any) {
        viewModelScope.launch {
            try {
                
                val jsonString = when (message) {
                    is String -> message
                    is JSONObject -> message.toString()
                    else -> return@launch
                }
                val jsonObject = JSONObject(jsonString)


                if (jsonObject.has("action")) {
                    val action = jsonObject.getJSONObject("action")
                    val actionType = action.optString("type")
                    val currentState = _uiState.value

                    when (actionType) {
                        "delete" -> {
                            val ids = mutableListOf<Long>()
                            val array = action.optJSONArray("messages")
                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    ids.add(array.getLong(i))
                                }
                            } else {
                                action.optLong("messageId").takeIf { it != 0L }?.let { ids.add(it) }
                            }
                            if (currentState is ChatDialogUiState.Success && ids.isNotEmpty()) {
                                val updatedChats = currentState.chats.filterNot { ids.contains(it.id) }
                                _uiState.value = currentState.copy(chats = updatedChats)
                            }
                        }
                        "read" -> {
                            val messageStatus = action.optInt("messageStatus", 0)
                            val ids = mutableListOf<Long>()
                            val array = action.optJSONArray("messages")
                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    ids.add(array.getLong(i))
                                }
                            }
                            if (currentState is ChatDialogUiState.Success && ids.isNotEmpty()) {
                                val updatedChats = currentState.chats.map { msg ->
                                    if (ids.contains(msg.id)) msg.copy(readCount = messageStatus) else msg
                                }
                                _uiState.value = currentState.copy(chats = updatedChats)
                            }
                        }
                        "update" -> {
                            val messageId = action.optLong("messageId", 0L)
                            if (currentState is ChatDialogUiState.Success && messageId != 0L) {
                                val gson = Gson()
                                val socketMessage = gson.fromJson(jsonString, ChatSocketMessage::class.java)
                                val updatedChats = currentState.chats.map { msg ->
                                    if (msg.id == messageId) {
                                        msg.copy(
                                            text = socketMessage.text ?: msg.text,
                                            // Динамическое обновление опроса: когда другой участник
                                            // голосует, бэк шлёт update-сообщение с актуальным poll
                                            // (новые проценты pv и aa текущего пользователя). Раньше
                                            // это поле игнорировалось и опрос не обновлялся вживую.
                                            poll = socketMessage.poll?.toDomain(msg.id, msg.text)
                                                ?: msg.poll
                                        )
                                    } else msg
                                }
                                _uiState.value = currentState.copy(chats = updatedChats)
                            }
                        }
                    }
                    return@launch
                }

                val gson = Gson()
                val socketMessage = gson.fromJson(jsonString, ChatSocketMessage::class.java)

                val isCallSignal = socketMessage.cType in listOf(2, 3) &&
                    socketMessage.files.isNullOrEmpty() &&
                    socketMessage.text?.contains("звонок", ignoreCase = true) == true
                val isCallEnded = socketMessage.cType == 6
                if (isCallSignal || isCallEnded) {
                    updateActiveCallFromSignal(started = isCallSignal, cType = socketMessage.cType)
                }

                if ((currentDialogInfo?.id ?: 0L) == 0L && (socketMessage.dialog ?: 0L) != 0L) {
                    currentDialogInfo = currentDialogInfo?.copy(id = socketMessage.dialog!!)
                    currentDialogID = socketMessage.dialog
                }

                val replyPreview = socketMessage.ansPreview?.let { preview ->
                    MessageChat(
                        id = preview.i,
                        text = preview.t,
                        type = MessageType.TEXT,
                        files = emptyList(),
                        ownerId = preview.o?.i,
                        createdAt = Instant.now(),
                        readCount = 0,
                        ownerName = preview.o?.fn,
                        ownerAvatarUrl = preview.o?.im,
                        ownerIsAdmin = false,
                        isMine = preview.o?.i == currentUser?.id,
                        replyTo = null
                    )
                }

                val isForwarded = socketMessage.forwarded != null || !socketMessage.forwardedn.isNullOrEmpty()
                val forwardAuthor = if (isForwarded) {
                    lastSentForwardAuthor.also { lastSentForwardAuthor = null }
                } else null

                val newMessage = socketMessage
                    .toMessageChat(replyPreview, forwardAuthor)
                    .let { message ->
                        currentDialogInfo?.let { info ->
                            message.updateOwnerInfoFromDialog(info)
                        } ?: message
                    }

                val currentState = _uiState.value
                if (currentState is ChatDialogUiState.Success) {
                    val existingIndex = currentState.chats.indexOfFirst { it.id == newMessage.id }
                    val updatedChats = if (existingIndex >= 0) {
                        currentState.chats.toMutableList().apply {
                            set(existingIndex, newMessage)
                        }
                    } else {
                        currentState.chats.toMutableList().apply {
                            add(newMessage)
                        }
                    }
                    _uiState.value = currentState.copy(chats = updatedChats)
                }

                if (!newMessage.isMine && newMessage.id != 0L) {
                    val dialogId = socketMessage.dialog ?: currentDialogID ?: return@launch
                    markMessagesRead(dialogId, listOf(newMessage))
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error processing incoming message", e)
            }
        }
    }

    private fun ChatSocketMessage.toMessageChat(
        replyPreview: MessageChat?,
        forwardedFromName: String? = null,
    ): MessageChat {
        val createdAtInstant = parseInstantOrNow(createdAt)
        val attachments = files?.mapNotNull { it.toChatFile() } ?: emptyList()
        val isMineMessage = owner == currentUser?.id
        val type = when (cType) {
            // 6003 "Вызов пропущен" / 6004 "Звонок пропущен" — системные события
            // звонков, как и 2/3/5/6. Показываем по центру, а не баблом.
            2, 3, 5, 6, 6003, 6004 -> MessageType.SYSTEM
            4 -> MessageType.VOICE
            9 -> MessageType.POLL
            else -> MessageType.TEXT
        }
        val pollDomain = poll?.toDomain(id, text)
        val isForwarded = forwarded != null || !forwardedn.isNullOrEmpty()

        return MessageChat(
            id = id ?: 0L,
            text = text,
            type = type,
            files = attachments,
            ownerId = owner,
            createdAt = createdAtInstant,
            readCount = read ?: if (isMineMessage) 1 else 0,
            ownerName = ownerName,
            ownerAvatarUrl = ownerAvatarUrl,
            ownerIsAdmin = false,
            isMine = isMineMessage,
            replyTo = replyPreview,
            poll = pollDomain,
            forwardedFromName = if (isForwarded) forwardedFromName else null,
        )
    }

    private fun FileDescriptor.toChatFile(): ChatFile? {
        val filePath = path ?: return null
        return ChatFile(
            id = id,
            path = filePath,
            fileName = fileName,
            contentType = contentType,
            fileSize = fileSize,
            fileType = fileType,
            fileKind = fileKind,
            duration = duration,
            viewCount = viewCount
        )
    }

    private fun parseInstantOrNow(value: String?): Instant {
        if (value.isNullOrBlank()) return Instant.now()
        return runCatching { Instant.parse(value) }.getOrElse { Instant.now() }
    }

    override fun onCleared() {
        socketService.removeEvent("message", LISTENER_TAG)
        socketService.removeEvent(SOCIAL_EVENT, SOCIAL_TAG)
        typingIdleStopJob?.cancel()
        unsubscribeSocial()
        super.onCleared()
    }

    companion object {
        private const val LISTENER_TAG = "ChatDialogViewModel"
        private const val PAGE_SIZE = 30
        // Активность чата идёт по отдельному socket-каналу `social` (wiki «Social»).
        private const val SOCIAL_EVENT = "social"
        private const val SOCIAL_TAG = "ChatDialogSocial"
        // uref чата: «312:<chatId>» (312 — тип объекта «чат»).
        private const val CHAT_UREF_TYPE = "312"
        // Справочник cSubType #189: 3 = пишет сообщение, 7 = действие прекращено.
        private const val ACTIVITY_DICT = "189"
        private const val ACT_TYPING = 3
        private const val ACT_STOP = 7
        // Серверная подписка живёт ~1 мин — продлеваем раньше срока.
        private const val SUBSTATS_TTL_MS = 45_000L
        // Автор шлёт 189:3 не чаще раза в 3с; при простое 5с — авто-стоп 189:7.
        private const val TYPING_PING_MS = 3_000L
        private const val TYPING_IDLE_STOP_MS = 5_000L
        // Подписчик снимает статус, если событий нет 5-7с.
        private const val TYPING_TTL_MS = 6_000L
        // После 189:7 игнорируем 189:3 того же автора в пределах <1с (дедуп).
        private const val STOP_DEBOUNCE_MS = 1_000L
    }
}

/**
 * Активность собеседника в чате: кто (`user`) и что делает (`action` — код из
 * справочника #189: 1 видео, 2 голос, 3 пишет, 4 файл, 5 фото, 6 аудио).
 * Питает индикатор в ленте и строку статуса в шапке.
 */
data class ChatActivity(val user: User, val action: Int)

private fun ChatPoll.toDomain(messageId: Long?, questionFallback: String?): Poll {
    val sortedOptions = options.orEmpty().sortedBy { it.order ?: Int.MAX_VALUE }
    val answeredIds = answeredOptionIds.orEmpty().toSet()
    return Poll(
        id = id.orEmpty(),
        dialogId = null,
        messageId = messageId,
        subject = (subject ?: questionFallback).orEmpty(),
        // Anonymity is not part of the chat preview — it only appears in the
        // author-only REST detail (GET /dialogs/poll/:id).
        isAnonymous = false,
        multipleAnswers = multipleAnswers ?: false,
        isQuiz = isQuiz ?: false,
        // `a == false` means the poll is finished; a missing `a` stays active.
        isStopped = isActive == false,
        options = sortedOptions.map { it.toDomain(answeredIds) },
        authorId = author?.id,
    )
}

private fun ChatPollOption.toDomain(answeredOptionIds: Set<String>): PollOption = PollOption(
    id = id.orEmpty(),
    value = value.orEmpty(),
    description = description,
    isRightAnswer = isRightAnswer,
    // The chat preview carries percentages (`pv`), not raw vote counts.
    voteCount = 0,
    percent = percent,
    isVoted = id != null && id in answeredOptionIds,
    answeredUsers = emptyList()
)

private const val READ_STATUS = 3

/** Статус участника звонка «Участвует» (см. docs/calls.md). */
private const val CALL_STATUS_PARTICIPATING = 5

private fun computeIsCurrentUserAdmin(info: DialogInfo, currentUserId: String?): Boolean {
    if (currentUserId.isNullOrEmpty()) return false
    return info.users.orEmpty().any { user ->
        user.userId == currentUserId && (user.isAdmin || isOwnerRole(user.role) || isAdminRole(user.role))
    }
}

private fun isOwnerRole(role: String?): Boolean {
    if (role.isNullOrBlank()) return false
    val normalized = role.lowercase()
    return normalized == "37:201" || normalized.contains("owner") || normalized.contains("влад")
}

private fun isAdminRole(role: String?): Boolean {
    if (role.isNullOrBlank()) return false
    val normalized = role.lowercase()
    return normalized == "37:202" || normalized.contains("admin") || normalized.contains("админ")
}

sealed class ChatDialogEvents {
    data class OpenChatProfileDetail(val dialogId: Long, val dialogInfo: DialogInfo) :
        ChatDialogEvents()

    // Открыть профиль пользователя (тап по @упоминанию). Профиль уже загружен
    // через GET core/user_profile/:userId.
    data class OpenUserProfile(val profile: UserProfileFullInfo) : ChatDialogEvents()
}

sealed class ChatDialogUiState {
    data class Loading(
        val chatName: String = "",
        val chatImage: String = "",
        val isGroup: Boolean = false,
        val firstParticipantName: String = "",
        val status: String? = null,
    ) : ChatDialogUiState()

    data class Success(
        val chats: List<MessageChat>,
        val chatName: String,
        val chatImage: String,
        val isGroup: Boolean,
        val firstParticipantName: String = "",
        val currentMessage: String = "",
        val attachedFiles: List<File> = emptyList(),
        val status: String? = null,
        val replyMessage: MessageChat? = null,
        val attachedContact: ContactInfo? = null,
        val selectedContact: UserProfileFullInfo? = null,
        val editingMessage: MessageChat? = null,
        val pendingForward: PendingForward? = null,
        val pinnedMessages: List<uddug.com.domain.entities.chat.PinnedMessagePreview> = emptyList(),
        val activeCall: ActiveCall? = null,
        val activeCallParticipantsCount: Int = 0,
        val activeCallMembers: List<CallMemberPreview> = emptyList(),
    ) : ChatDialogUiState()

    data class Error(val message: String) : ChatDialogUiState()
}

/** Участник активного звонка для кластера аватарок в баннере «К звонку». */
data class CallMemberPreview(
    val name: String?,
    val imageUrl: String?,
)

data class PendingForward(
    val messageIds: List<Long>,
    val text: String?,
    val authorName: String?,
)
