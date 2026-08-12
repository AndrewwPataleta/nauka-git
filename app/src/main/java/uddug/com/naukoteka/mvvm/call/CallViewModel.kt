package uddug.com.naukoteka.mvvm.call

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashphoner.fpwcsapi.bean.Connection
import com.flashphoner.fpwcsapi.constraints.Constraints
import com.flashphoner.fpwcsapi.handler.CameraSwitchHandler
import com.flashphoner.fpwcsapi.room.Message
import com.flashphoner.fpwcsapi.room.Participant
import com.flashphoner.fpwcsapi.room.Room
import com.flashphoner.fpwcsapi.room.RoomEvent
import com.flashphoner.fpwcsapi.room.RoomManagerEvent
import com.flashphoner.fpwcsapi.session.Stream
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.webrtc.SurfaceViewRenderer
import retrofit2.HttpException
import java.util.UUID
import com.google.gson.Gson
import org.json.JSONObject
import uddug.com.domain.entities.call.CallSessionState
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.call.CallRepository
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.naukoteka.flashphoner.FlashphonerConfig
import uddug.com.naukoteka.flashphoner.FlashphonerConfigProvider
import uddug.com.naukoteka.flashphoner.FlashphonerSessionManager
import uddug.com.naukoteka.mvvm.chat.await

@HiltViewModel
class CallViewModel @Inject constructor(
    private val flashphonerConfigProvider: FlashphonerConfigProvider,
    private val flashphonerSessionManager: FlashphonerSessionManager,
    private val userProfileRepository: UserProfileRepository,
    private val callRepository: CallRepository,
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService,
    private val callAudioManager: CallAudioManager,
    private val incomingCallStore: IncomingCallStore,
    private val activeCallStore: ActiveCallStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val toastEvents: SharedFlow<String> = _toastEvents

    private val gson = Gson()

    init {
        socketService.setOnEvent("message", SOCKET_LISTENER_TAG) { message ->
            viewModelScope.launch { handleSocketMessage(message) }
        }
        callAudioManager.onRoutesChanged = { refreshAudioRoutes() }
    }

    private var isFrontCamera: Boolean = true

    private var isCallStarted = false
    private var callDurationJob: Job? = null
    private var vuMeterJob: Job? = null
    // Последние замеры уровня звука по участнику (audioLevel 0..1 из WebRTC
    // getStats). Пишется из колбэков getStats (webrtc-поток), читается в тике
    // VU-метра — потому ConcurrentHashMap.
    private val speakingLevels = java.util.concurrent.ConcurrentHashMap<String, Double>()
    private var callStartedAtMs: Long? = null
    private var mediaSessionId: String? = null
    private var lastCallParams: CallParams? = null
    private var reconnectAttempts = 0
    // Поколение звонка: растёт на каждый старт. Отложенный «мягкий» disconnect
    // при выходе (см. endCall) срабатывает, только если поколение не изменилось —
    // иначе быстрый повторный вход («К звонку» за <500мс) уронил бы новую сессию.
    private var callGeneration = 0
    private var remoteRenderer: SurfaceViewRenderer? = null
    private val participantStreams = mutableMapOf<String, Stream>()
    private val participantRenderers = mutableMapOf<String, SurfaceViewRenderer>()
    private val participantHandles = mutableMapOf<String, Participant>()
    private val remoteResubscribeAttempts = mutableMapOf<String, Int>()
    private val remoteResubscribeJobs = mutableMapOf<String, Job>()
    private var primaryStreamKey: String? = null
    private var localRenderer: SurfaceViewRenderer? = null
    private var localStream: Stream? = null
    private var remoteStream: Stream? = null
    private var pendingRemoteParticipant: Participant? = null
    private var callOperationId: String? = null
    private var lastParticipantsCount: Int = 0
    private var participantWatchdogJob: Job? = null
    private var currentConfig: FlashphonerConfig? = null
    private var currentRoomName: String? = null
    private var currentStreamName: String? = null
    private var currentLogin: String? = null
    private var localPublishStarted = false
    // Опубликован ли локальный поток С видео-дорожкой (даже если камера
    // выключена — дорожка есть, но muteVideo). В групповых звонках публикуем
    // видео сразу (замьюченным), чтобы включение камеры было простым unmuteVideo,
    // а не разрушительным unpublish→republish (последний заставляет сервер
    // считать нас вышедшими и завершает звонок — источник «включаю камеру → всё
    // падает»). false — упали в аудио-only (нет права на камеру).
    private var publishedWithVideo = false
    private var hasPublishedLocalStream = false
    private var hasRetriedPublish = false
    // Счётчик и джоба backoff-переиздания локального потока после FAILED (чтобы
    // пережить призрачную сессию с тем же именем на сервере).
    private var localRepublishAttempts = 0
    private var localRepublishJob: Job? = null
    // true, когда после реджойна бэкенд терминировал нашу публикацию и мы отправили
    // 2002 (ждём разрешения из лобби). В это время не долбим republish — ждём 2004/2005.
    private var awaitingJoinApproval = false
    private var profileUserId: String? = null

    fun showIncomingCall(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        participants: List<CallParticipant>? = null,
        callTitle: String? = null,
        isVideoCall: Boolean = false,
        isGroupCall: Boolean = false,
    ) {
        if (isCallStarted || _uiState.value.status == CallStatus.IN_CALL) return

        if (dialogId <= 0) {
            _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
            return
        }

        // For a group call the tiles come purely from Flashphoner room events;
        // a synthetic contact-name participant would otherwise linger as a
        // bogus "<group name>" tile in the participants list.
        val resolvedParticipants = if (isGroupCall) {
            emptyList()
        } else {
            participants?.takeIf { it.isNotEmpty() }
                ?: contactName?.let { name ->
                    listOf(
                        CallParticipant(
                            id = name,
                            name = name,
                            avatarUrl = avatarUrl,
                        )
                    )
                }
                ?: emptyList()
        }

        lastCallParams = CallParams(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
            isGroupCall = isGroupCall,
        )

        reconnectAttempts = 0
        isCallStarted = false

        _uiState.value = CallUiState(
            dialogId = dialogId,
            callTitle = callTitle ?: contactName,
            participants = resolvedParticipants,
            status = CallStatus.INCOMING,
            isVideoCall = isVideoCall,
            sessionState = CallSessionState(micOn = true, camOn = isVideoCall),
            isRecording = false,
            errorMessage = null,
            isGroupCall = isGroupCall,
        )
    }

    fun startCall(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        participants: List<CallParticipant>? = null,
        callTitle: String? = null,
        isVideoCall: Boolean = true,
        resetReconnectAttempts: Boolean = true,
        isAcceptingIncomingCall: Boolean = false,
        isGroupCall: Boolean = false,
    ) {
        if (isCallStarted) {
            if (!flashphonerSessionManager.isRoomJoined()) {
                logCallStep("rejoin_stale_session", "resetting dead session for dialogId=$dialogId")
                isCallStarted = false
                flashphonerSessionManager.reset()
            } else {
                return
            }
        }

        if (resetReconnectAttempts) {
            reconnectAttempts = 0
            // Fresh call (not a reconnect) — count the duration from scratch.
            callStartedAtMs = null
        }
        lastCallParams = CallParams(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
            isGroupCall = isGroupCall,
        )

        if (dialogId <= 0) {
            _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
            return
        }
        isCallStarted = true
        callGeneration++
        lastParticipantsCount = 0
        localPublishStarted = false
        publishedWithVideo = false
        hasPublishedLocalStream = false
        hasRetriedPublish = false
        localRepublishAttempts = 0
        awaitingJoinApproval = false
        localRepublishJob?.cancel()

        val resolvedParticipants = if (isGroupCall) {
            emptyList()
        } else {
            participants?.takeIf { it.isNotEmpty() }
                ?: contactName?.let { name ->
                    listOf(
                        CallParticipant(
                            id = name,
                            name = name,
                            avatarUrl = avatarUrl,
                        )
                    )
                }
                ?: emptyList()
        }

        val initialStatus = if (isAcceptingIncomingCall) {
            CallStatus.CONNECTING
        } else {
            CallStatus.DIALING
        }

        _uiState.value = CallUiState(
            dialogId = dialogId,
            callTitle = callTitle ?: contactName,
            participants = resolvedParticipants,
            status = initialStatus,
            isVideoCall = isVideoCall,
            sessionState = CallSessionState(micOn = true, camOn = isVideoCall),
            isRecording = false,
            errorMessage = null,
            isGroupCall = isGroupCall,
        )

        // On a reconnect the call was already counting — restart the ticking
        // job so the duration doesn't freeze after the _uiState reset above.
        // The value is derived from callStartedAtMs, which is preserved.
        if (callStartedAtMs != null) {
            startCallTimer()
        }

        if (isGroupCall) {
            refreshParticipants()
        }

        callAudioManager.acquire()
        refreshAudioRoutes()

        viewModelScope.launch {
            runCatching {
                val config = flashphonerConfigProvider.defaultConfig
                val username = resolveWcsLogin()
                val streamName = buildStreamName(dialogId, username)
                val operationId = UUID.randomUUID().toString()

                callOperationId = operationId
                currentLogin = username
                currentConfig = config
                currentRoomName = dialogId.toString()
                currentStreamName = streamName
                mediaSessionId = streamName

                logCallStep(
                    "stream_name_constructed",
                    "dialogId=$dialogId username=$username constructedStream=$streamName"
                )

                if (config.streamName.isBlank()) {
                    logCallStep("sanity_stream_name_blank", "streamName is blank")
                }
                if (currentRoomName.isNullOrBlank()) {
                    logCallStep("sanity_room_name_blank", "roomName is blank")
                }
                if (username.isBlank()) {
                    logCallStep("sanity_username_blank", "username is blank")
                }

                logCallStep(
                    "connect_ws_start",
                    "serverUrl=${config.serverUrl} username=$username streamName=$streamName roomName=$dialogId"
                )
                logWcsDiagnostics(
                    selectedLogin = username,
                    serverUrl = config.serverUrl,
                    streamName = streamName,
                    dialogId = dialogId,
                )

                flashphonerSessionManager.prepareRoomManager(
                    serverUrl = config.serverUrl,
                    username = username,
                )

                flashphonerSessionManager.connectRoomManager(
                    createRoomManagerEvent(dialogId, streamName, isVideoCall)
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
                startParticipantWatchdog()
                if (isGroupCall) {
                    refreshParticipants()
                }
            }.onFailure {
                handleCallFailure("Failed to connect to call service.")
            }
        }
    }

    /**
     * Завершить звонок ДЛЯ ВСЕХ (REST `POST /calls/dialog/:dialog/stop`, см.
     * docs/calls.md §7). Бэк рассылает cType 6 всем участникам — у собеседника
     * звонок закрывается сразу, а не висит до таймаута зависшего звонка.
     * Используется в 1-на-1 (кнопка «Завершить» = завершить у обоих) и как
     * «Завершить для всех» у администратора/организатора группового звонка.
     * После REST локально всё равно чистим состояние через [endCall].
     */
    fun endCallForEveryone() {
        val dialogId = _uiState.value.dialogId
        if (dialogId != null) {
            viewModelScope.launch {
                runCatching { callRepository.stopDialogCall(dialogId) }
                    .onSuccess { logCallStep("stop_dialog_call_ok", "dialogId=$dialogId") }
                    .onFailure { logCallStep("stop_dialog_call_failed", "error=${it.message}") }
            }
        }
        endCall()
    }

    fun endCall() {
        val previousStatus = _uiState.value.status
        val endedDialogId = _uiState.value.dialogId
        // Declining an incoming call happens before joining the WCS room, so
        // there is no media session for the server to detect dropping. Without
        // an explicit signal the caller waits out the 30s no-answer timeout.
        // Send cType 6001 (CALL_DECLINED) so the caller's call ends at once.
        // See docs/calls.md §2.1.
        if (previousStatus == CallStatus.INCOMING && endedDialogId != null) {
            notifyIncomingCallDeclined(endedDialogId)
        }
        // Drop the pending-call record so ContainerActivity.onStart() can't
        // re-open the call screen on every resume/unlock after the call ended.
        incomingCallStore.clear()
        activeCallStore.clear()
        lastCallParams = null
        clearVideoStreams()
        // Чистый выход из комнаты: сперва unpublish + Room.leave (сервер снимает
        // нашего паблишера), и только ПОСЛЕ паузы закрываем WS-сессию. Иначе
        // disconnect обгоняет leave-сообщение, сервер держит «призрака» с тем же
        // именем потока, и повторный вход («К звонку») не публикуется —
        // STREAM_NAME_ALREADY_IN_USE, «меня не видят». См. docs/calls.md + ресерч.
        runCatching { flashphonerSessionManager.unpublishCurrentStream() }
        runCatching { flashphonerSessionManager.leaveRoom() }
        val leaveGeneration = callGeneration
        viewModelScope.launch {
            delay(500)
            // Не закрываем сессию, если за это время начался новый звонок
            // («К звонку» сразу после выхода) — иначе уроним свежую сессию.
            if (callGeneration == leaveGeneration) {
                runCatching { flashphonerSessionManager.disconnectRoom() }
            } else {
                logCallStep("leave_disconnect_skipped", "new call started (gen changed)")
            }
        }
        callAudioManager.release()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
            errorMessage = null,
        )
        stopCallTimer()
        isCallStarted = false
        stopParticipantWatchdog()
        localPublishStarted = false
        publishedWithVideo = false
        callOperationId = null
        currentConfig = null
        currentRoomName = null
        currentStreamName = null
        currentLogin = null
        profileUserId = null
        hasPublishedLocalStream = false
        hasRetriedPublish = false
        localRepublishAttempts = 0
        awaitingJoinApproval = false
        localRepublishJob?.cancel()
        clearVideoState()
    }

    /**
     * Запрос на присоединение к активному звонку (docs/calls.md §4). Нужен при
     * повторном входе после выхода: бэкенд ставит вышедшему терминальный статус 2
     * и терминирует прямую публикацию (info=Stopped by rest /terminate). Правильный
     * путь — отправить 2002, попасть в комнату ожидания (status 6) и дождаться
     * разрешения (2004/2005), после чего опубликоваться (joinFromLobby).
     */
    private fun requestJoinCall(dialogId: Long) {
        viewModelScope.launch {
            runCatching {
                val ownerId = profileUserId
                    ?: withContext(Dispatchers.IO) {
                        userProfileRepository.getProfileInfo().await()
                    }.id
                socketService.sendMessage(
                    "message",
                    ChatSocketMessage(
                        dialog = dialogId,
                        cType = CTYPE_JOIN_CALL,
                        owner = ownerId,
                    ),
                )
                logCallStep("join_call_requested", "dialogId=$dialogId (2002)")
            }.onFailure {
                logCallStep("join_call_request_failed", "error=${it.message}")
            }
        }
    }

    private fun notifyIncomingCallDeclined(dialogId: Long) {
        viewModelScope.launch {
            runCatching {
                val ownerId = profileUserId
                    ?: withContext(Dispatchers.IO) {
                        userProfileRepository.getProfileInfo().await()
                    }.id
                socketService.sendMessage(
                    "message",
                    ChatSocketMessage(
                        dialog = dialogId,
                        cType = CTYPE_CALL_DECLINED,
                        owner = ownerId,
                    ),
                )
                logCallStep("incoming_call_declined_sent", "dialogId=$dialogId")
            }.onFailure {
                logCallStep("incoming_call_declined_failed", "error=${it.message}")
            }
        }
    }

    fun ensureSpeakerphoneOn() {
        callAudioManager.ensureSpeakerphoneOn()
    }

    /**
     * Called from fragment's onResume. Ensures local/remote streams and renderers
     * are healthy after return from background/screen lock.
     */
    fun onAppResumed() {
        if (!isCallStarted) return
        val callStatus = _uiState.value.status
        if (callStatus != CallStatus.CONNECTING && callStatus != CallStatus.IN_CALL) return

        logCallStep(
            "app_resumed",
            "localStatus=${localStream?.status} remoteStatus=${remoteStream?.status} participantStreams=${participantStreams.size}"
        )

        // 1) Re-subscribe remote participants. A fresh play() re-renders video
        // reliably; re-attaching an existing stream via switchRenderer() often
        // leaves a black tile after returning from background or screen lock
        // (the "участников не видно" symptom). Fall back to switchRenderer only
        // when we have no live participant handle to replay from.
        participantRenderers.forEach { (participantId, renderer) ->
            val handle = participantHandles[participantId]
            if (handle != null) {
                playParticipant(handle, participantId, renderer)
            } else {
                findStreamForParticipant(participantId)?.let { stream ->
                    runCatching { stream.switchRenderer(renderer) }.onFailure {
                        logCallStep(
                            "resume_switch_renderer_failed",
                            "participantId=$participantId error=${it.message}"
                        )
                    }
                }
            }
        }
        localRenderer?.let { renderer ->
            runCatching { localStream?.switchRenderer(renderer) }
        }

        // 2) If local stream is broken (e.g., camera access was lost on screen lock),
        // republish to restore audio/video for remote peers.
        val localStatusStr = localStream?.status?.toString().orEmpty().uppercase()
        val localIsHealthy = localStatusStr == "PUBLISHING" || localStatusStr == "PUBLISHED"
        if (!localIsHealthy && _uiState.value.isVideoCall) {
            logCallStep("resume_local_stream_restart", "status=$localStatusStr")
            val session = _uiState.value.sessionState
            restartLocalStream(audioEnabled = session.micOn, videoEnabled = session.camOn)
        }
    }

    private fun startCallTimer() {
        // Anchor the duration to a wall-clock start time. Deriving the value
        // from it (instead of an incrementing counter) keeps the timer correct
        // across a reconnect, where startCall() rebuilds _uiState from scratch.
        if (callStartedAtMs == null) {
            callStartedAtMs = System.currentTimeMillis()
        }
        callDurationJob?.cancel()
        callDurationJob = viewModelScope.launch {
            while (isActive) {
                val startedAt = callStartedAtMs ?: break
                val elapsed = ((System.currentTimeMillis() - startedAt) / 1000)
                    .toInt()
                    .coerceAtLeast(0)
                _uiState.value = _uiState.value.copy(callDurationSeconds = elapsed)
                delay(1_000)
            }
        }
        startVuMeter()
    }

    private fun stopCallTimer() {
        callDurationJob?.cancel()
        callDurationJob = null
        callStartedAtMs = null
        stopVuMeter()
    }

    /**
     * VU-метр: раз в ~200 мс опрашивает уровень звука публикуемого и удалённых
     * потоков (WebRTC audioLevel через Stream.getStats) и помечает «говорящих».
     * Порог отсекает фоновый шум. Своё значение учитываем только при включённом
     * микрофоне. UI рисует пульсацию на аватарке говорящего.
     */
    private fun startVuMeter() {
        vuMeterJob?.cancel()
        vuMeterJob = viewModelScope.launch {
            while (isActive) {
                requestAudioLevelSamples()
                val selfId = profileUserId
                val micOn = _uiState.value.sessionState.micOn
                val speakers = speakingLevels
                    .filter { it.value > SPEAKING_THRESHOLD }
                    .keys
                    .toMutableSet()
                val selfSpeaking = selfId != null &&
                    micOn &&
                    (speakingLevels[selfId] ?: 0.0) > SPEAKING_THRESHOLD
                if (selfId != null) speakers.remove(selfId)
                _uiState.value = _uiState.value.copy(
                    isSelfSpeaking = selfSpeaking,
                    speakingParticipantIds = speakers,
                )
                delay(VU_METER_INTERVAL_MS)
            }
        }
    }

    private fun stopVuMeter() {
        vuMeterJob?.cancel()
        vuMeterJob = null
        speakingLevels.clear()
        _uiState.value = _uiState.value.copy(
            isSelfSpeaking = false,
            speakingParticipantIds = emptySet(),
        )
    }

    /**
     * Запрашивает уровень звука ТОЛЬКО у локального потока и ТОЛЬКО когда звонок
     * уже стабильно активен. Опрашивать удалённые потоки через getStats нельзя:
     * их PeerConnection может ещё договариваться (setRemoteSDP), и колбэк
     * getStats на signaling-треде роняет нативный WebRTC с
     * `Check failed: !env->ExceptionCheck()` (SIGABRT). Поэтому пульсацию
     * driver'им только для «я говорю».
     */
    private fun requestAudioLevelSamples() {
        if (_uiState.value.status != CallStatus.IN_CALL) return
        val selfId = profileUserId ?: return
        val local = localStream ?: return
        if (!hasPublishedLocalStream) return
        runCatching {
            local.getStats { stats ->
                runCatching { speakingLevels[selfId] = audioLevelOf(stats) }
            }
        }.onFailure { logCallStep("vu_getstats_failed", "error=${it.message}") }
    }

    private fun audioLevelOf(stats: com.flashphoner.fpwcsapi.session.StreamStats?): Double {
        val rtc = stats?.audioStats ?: return 0.0
        val level = runCatching { rtc.members?.get("audioLevel") }.getOrNull()
        return (level as? Number)?.toDouble() ?: 0.0
    }

    private fun createRoomManagerEvent(
        dialogId: Long,
        streamName: String,
        isVideoCall: Boolean,
    ): RoomManagerEvent {
        return object : RoomManagerEvent {
            override fun onConnected(connection: Connection) {
                logCallStep(
                    "room_manager_connected",
                    "status=${connection.status} reconnectAttempts=$reconnectAttempts"
                )
                logCallStep("join_room_requested", "roomName=$dialogId")
                flashphonerSessionManager.joinRoom(
                    roomName = dialogId.toString(),
                    roomEvent = { room ->
                        room.on(
                            createRoomEvent(
                                dialogId = dialogId,
                                streamName = streamName,
                                isVideoCall = isVideoCall,
                            )
                        )
                    },
                )
            }

            override fun onDisconnection(connection: Connection) {
                logCallStep(
                    "room_manager_disconnected",
                    "status=${connection.status} reconnectAttempts=$reconnectAttempts"
                )
                attemptReconnectOrFail()
            }
        }
    }

    private fun createRoomEvent(
        dialogId: Long,
        streamName: String,
        isVideoCall: Boolean,
    ): RoomEvent {
        return object : RoomEvent {
            override fun onState(room: Room) {
                val remoteParticipants = room.participants.filterNot(::isSelfParticipant)
                lastParticipantsCount = remoteParticipants.size
                logCallStep(
                    "room_state",
                    "participants=${room.participants.size} remoteParticipants=${remoteParticipants.size} room=${room.name}"
                )
                remoteParticipants.forEach { addParticipantToUiState(it) }
                ensureLocalPublishStarted(streamName, isVideoCall, "room_state")
                subscribeToParticipants(remoteParticipants)
                checkParticipantWatchdog()
            }

            override fun onJoined(participant: Participant) {
                logCallStep("participant_joined", "participant=${participant.name}")
                if (isSelfParticipant(participant)) {
                    Log.d("CallVM", "JOINED ROOM: $dialogId")
                    publishLocalStream(streamName, isVideoCall)
                    return
                }
                lastParticipantsCount = maxOf(lastParticipantsCount, 1)
                addParticipantToUiState(participant)
                subscribeToParticipants(listOf(participant))
                checkParticipantWatchdog()
            }

            override fun onLeft(participant: Participant) {
                logCallStep("participant_left", "participant=${participant.name}")
                participant.name?.let { participantHandles.remove(it) }
                removeParticipantStream(participant)
                participant.stop()
                if (participant == pendingRemoteParticipant || participant.stream == remoteStream) {
                    pendingRemoteParticipant = null
                    remoteStream = null
                }
                if (!isSelfParticipant(participant)) {
                    val participantId = participant.name ?: return
                    val leftParticipant = _uiState.value.participants.find { it.id == participantId }
                    val displayName = leftParticipant?.name ?: participantId
                    _uiState.value = _uiState.value.copy(
                        participants = _uiState.value.participants.filter { it.id != participantId }
                    )
                    _toastEvents.tryEmit("$displayName покинул(а) звонок")
                }
            }

            override fun onPublished(participant: Participant) {
                val scope = if (isSelfParticipant(participant)) "local" else "remote"
                logCallStep("participant_published", "scope=$scope participant=${participant.name}")
                if (!isSelfParticipant(participant)) {
                    lastParticipantsCount = maxOf(lastParticipantsCount, 1)
                    addParticipantToUiState(participant)
                    forceResubscribeParticipant(participant)
                    checkParticipantWatchdog()
                }
            }

            override fun onFailed(room: Room, error: String) {
                Log.e("CallVM", "RoomEvent.onFailed error=$error")
                logCallStep("join_room_failed", "room=${room.name} error=$error")
                handleCallFailure("Room join failed: $error")
            }

            override fun onMessage(message: Message) = Unit
        }
    }

    private fun ensureLocalPublishStarted(
        streamName: String,
        isVideoCall: Boolean,
        source: String,
    ) {
        if (localPublishStarted) {
            logCallStep("publish_local_stream_skipped", "source=$source reason=already_started")
            return
        }
        logCallStep("join_room_confirmed", "source=$source streamName=$streamName")
        publishLocalStream(streamName, isVideoCall)
    }

    private fun publishLocalStream(streamName: String, isVideoCall: Boolean) {
        val audioEnabled = _uiState.value.sessionState.micOn
        logCallStep(
            "publish_local_stream_requested",
            "streamName=$streamName isVideoCall=$isVideoCall audioEnabled=$audioEnabled"
        )
        if (localPublishStarted) {
            logCallStep("publish_local_stream_skipped", "already_requested streamName=$streamName")
            return
        }
        if (hasPublishedLocalStream) {
            logCallStep("publish_local_stream_skipped", "already_published streamName=$streamName")
            return
        }
        localPublishStarted = true
        saveActiveCallState()

        // Публикуем видео-дорожку ТОЛЬКО когда звонок реально видео или камера
        // включена. Форсить видео в групповом аудиозвонке нельзя: на эмуляторе/
        // слабом железе видео-энкодер стабильно валит публикацию (PUBLISHING→
        // FAILED), поток рушится, сервер видит битого паблишера и завершает звонок
        // (cType 6) — «камера выкл, никто не видит, при включении всё падает».
        // Аудио публикуется стабильно → база звонка живёт; видео поднимаем только
        // по явному включению камеры (там уже есть аудио-фолбэк при провале).
        val isGroup = _uiState.value.isGroupCall
        val camOn = _uiState.value.sessionState.camOn
        val wantVideoTrack = isVideoCall || camOn
        logCallStep(
            "publish_local_stream_start",
            "streamName=$streamName isVideoCall=$isVideoCall isGroup=$isGroup wantVideoTrack=$wantVideoTrack audio=$audioEnabled camOn=$camOn"
        )
        Log.d("CallVM", "PUBLISHING STREAM: $streamName video=$wantVideoTrack")
        doPublish(streamName, audioEnabled, wantVideoTrack, camOn, allowAudioFallback = wantVideoTrack)
    }

    /**
     * Единая точка публикации локального потока с подробным логом каждого шага.
     * При [allowAudioFallback] и падении публикации с видео — повторяет попытку
     * без видео (частый случай: нет разрешения CAMERA), не роняя звонок.
     */
    private fun doPublish(
        streamName: String,
        audioEnabled: Boolean,
        videoEnabled: Boolean,
        camOn: Boolean,
        allowAudioFallback: Boolean,
    ) {
        logCallStep(
            "publish_attempt",
            "streamName=$streamName video=$videoEnabled audio=$audioEnabled camOn=$camOn fallback=$allowAudioFallback roomJoined=${flashphonerSessionManager.isRoomJoined()}"
        )
        runCatching {
            localStream = flashphonerSessionManager.publishToCurrentRoom(streamName, localRenderer) {
                constraints = Constraints(audioEnabled, videoEnabled)
            }
        }.onSuccess {
            publishedWithVideo = videoEnabled
            val actualMediaSessionId = localStream?.id
            if (!actualMediaSessionId.isNullOrBlank()) {
                mediaSessionId = actualMediaSessionId
            }
            logCallStep(
                "publish_success",
                "streamName=$streamName video=$videoEnabled actualName=${localStream?.name ?: "n/a"} msid=${localStream?.id ?: "n/a"}"
            )
            // Приводим дорожки к текущему UI-состоянию: если камера выключена —
            // мьютим видео (дорожка есть, но не транслируется); если микрофон
            // выключен — мьютим аудио.
            if (videoEnabled && !camOn) {
                runCatching { localStream?.muteVideo() }
                    .onFailure { logCallStep("post_publish_mute_video_failed", "error=${it.message}") }
            }
            if (!audioEnabled) {
                runCatching { localStream?.muteAudio() }
                    .onFailure { logCallStep("post_publish_mute_audio_failed", "error=${it.message}") }
            }
            attachStreamDiagnostics(localStream, "local_publish")
            if (videoEnabled) {
                refreshCameras()
                applyPreferredCamera()
            }
            _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
        }.onFailure { error ->
            Log.e("CallVM", "publish failed video=$videoEnabled", error)
            logCallStep("publish_failed", "streamName=$streamName video=$videoEnabled error=${error.message}")
            if (allowAudioFallback && videoEnabled) {
                // Не роняем звонок: пробуем аудио-only (нет права CAMERA и т.п.).
                logCallStep("publish_audio_fallback", "retrying without video")
                doPublish(streamName, audioEnabled, videoEnabled = false, camOn = false, allowAudioFallback = false)
            } else {
                localPublishStarted = false
                publishedWithVideo = false
                logCallStep("publish_local_stream_failure", "streamName=$streamName error=${error.message}")
                handleCallFailure("Failed to publish local media.")
            }
        }
    }

    private var restartStreamJob: Job? = null

    private fun restartLocalStream(audioEnabled: Boolean, videoEnabled: Boolean) {
        val streamName = currentStreamName ?: return

        restartStreamJob?.cancel()
        restartStreamJob = viewModelScope.launch {
            // Guard: never try to unpublish/publish without a joined room.
            // Mic/camera toggles can race ahead of onConnected→joinRoom in the
            // initial CONNECTING phase, or arrive after room disconnect.
            if (!flashphonerSessionManager.isRoomJoined()) {
                logCallStep("restart_skipped_no_room", "streamName=$streamName")
                return@launch
            }

            runCatching {
                flashphonerSessionManager.unpublishCurrentStream()
            }.onFailure {
                logCallStep("restart_unpublish_failed", "error=${it.message}")
            }

            // Delay to let Flashphoner finalize unpublish before republishing.
            // Without this, native layer can hit a race and throw.
            delay(STREAM_RESTART_DELAY_MS)

            // Room state might have changed while we were waiting (e.g., a
            // disconnect event fired during the delay). Re-check before publish.
            if (!flashphonerSessionManager.isRoomJoined()) {
                logCallStep("restart_aborted_room_lost", "streamName=$streamName")
                localPublishStarted = false
                localStream = null
                return@launch
            }

            logCallStep(
                "publish_local_stream_restart_params",
                "roomName=${currentRoomName ?: "n/a"} mediaSessionId=${mediaSessionId ?: "n/a"} streamName=$streamName customParams=constraints(audio=$audioEnabled,video=$videoEnabled)"
            )
            runCatching {
                localStream = flashphonerSessionManager.publishToCurrentRoom(streamName, localRenderer) {
                    constraints = Constraints(audioEnabled, videoEnabled)
                }
            }.onSuccess {
                val actualMediaSessionId = localStream?.id
                if (!actualMediaSessionId.isNullOrBlank()) {
                    mediaSessionId = actualMediaSessionId
                }
                hasPublishedLocalStream = false
                localPublishStarted = true
                publishedWithVideo = videoEnabled
                logCallStep(
                    "publish_local_stream_restart_identifiers",
                    "requestedStreamName=$streamName actualStreamName=${localStream?.name ?: "n/a"} actualMediaSessionId=${localStream?.id ?: "n/a"} video=$videoEnabled"
                )
                attachStreamDiagnostics(localStream, "local_publish")
                if (videoEnabled) applyPreferredCamera()
            }.onFailure {
                Log.e("CallVM", "restartLocalStream failed", it)
                logCallStep("restart_republish_failed", "error=${it.message}")
                localPublishStarted = false
                // Do NOT end the call — just leave the user without their local stream.
                // User can retry by toggling camera/mic again.
            }
        }
    }

    private fun attemptReconnectOrFail() {
        val params = lastCallParams
        if (params == null || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            logCallStep(
                "reconnect_aborted",
                "paramsAvailable=${params != null} attempts=$reconnectAttempts"
            )
            handleCallFailure("Call disconnected.")
            return
        }

        reconnectAttempts++
        val attempt = reconnectAttempts
        logCallStep("reconnect_attempt", "attempt=$attempt")
        isCallStarted = false
        flashphonerSessionManager.reset()
        // Back off before rejoining: a WCS session that dropped on a transient
        // network blip lingers server-side for a few seconds. Rejoining
        // immediately republishes under the same stream name and FAILs against
        // the ghost session ("I rejoined but nobody hears me"). The delay grows
        // per attempt so later retries wait longer for the ghost to time out.
        viewModelScope.launch {
            delay(RECONNECT_BASE_DELAY_MS * attempt)
            // The user may have hung up (or the server ended the call) during
            // the back-off — lastCallParams is nulled on end. Bail if so.
            val stillActive = lastCallParams
            if (stillActive == null) {
                logCallStep("reconnect_cancelled", "call ended during backoff attempt=$attempt")
                return@launch
            }
            startCall(
                dialogId = stillActive.dialogId,
                contactName = stillActive.contactName,
                avatarUrl = stillActive.avatarUrl,
                participants = stillActive.participants,
                callTitle = stillActive.callTitle,
                isVideoCall = stillActive.isVideoCall,
                resetReconnectAttempts = false,
                isGroupCall = stillActive.isGroupCall,
            )
        }
    }

    fun onAudioFocusFailed(message: String) {
        logCallStep("audio_focus_failed", message)
        handleCallFailure(message)
    }

    fun onMicPermissionDenied() {
        logCallStep("mic_permission_denied", "RECORD_AUDIO not granted")
        handleCallFailure("Microphone permission is required to start the call.")
    }

    fun toggleMicrophone() {
        val currentState = _uiState.value.sessionState
        val updatedState = currentState.copy(micOn = !currentState.micOn)
        _uiState.value = _uiState.value.copy(sessionState = updatedState)
        updateCallState(updatedState)
        applyLocalAudioState(updatedState.micOn)
    }

    fun toggleCamera() {
        val currentState = _uiState.value.sessionState
        val enabling = !currentState.camOn
        val updatedState = currentState.copy(camOn = enabling)
        _uiState.value = _uiState.value.copy(sessionState = updatedState)
        updateCallState(updatedState)
        logCallStep(
            "toggle_camera",
            "enabling=$enabling publishedWithVideo=$publishedWithVideo isVideoCall=${_uiState.value.isVideoCall} roomJoined=${flashphonerSessionManager.isRoomJoined()}"
        )

        // Видео-дорожка уже опубликована (в группе публикуем её сразу): просто
        // мьютим/размьютим — сессия не рвётся, звонок не падает. Помечаем звонок
        // видео, чтобы сетка переключилась и переживала реконнект.
        if (publishedWithVideo) {
            if (enabling && !_uiState.value.isVideoCall) {
                _uiState.value = _uiState.value.copy(isVideoCall = true)
                lastCallParams = lastCallParams?.copy(isVideoCall = true)
                saveActiveCallState()
            }
            applyLocalVideoState(updatedState.camOn)
            return
        }

        // Фолбэк (публиковались аудио-only, напр. не было права CAMERA): дорожки
        // видео нет, нужен республиш. Это разрушительный путь (unpublish→publish),
        // поэтому по возможности его избегаем — сюда попадаем только если старт
        // прошёл без видео.
        if (enabling) {
            logCallStep("camera_upgrade_republish", "no video track, republishing (fallback)")
            _uiState.value = _uiState.value.copy(isVideoCall = true)
            lastCallParams = lastCallParams?.copy(isVideoCall = true)
            saveActiveCallState()
            restartLocalStream(audioEnabled = updatedState.micOn, videoEnabled = true)
        } else {
            applyLocalVideoState(updatedState.camOn)
        }
    }

    /**
     * Persists the current call so it can be restored after a process death
     * ([ActiveCallStore]). Kept in sync with [lastCallParams] — in particular its
     * isVideoCall flag, which is bumped to true when the user upgrades an audio
     * call to video (see [toggleCamera]) so the restored call keeps video.
     */
    private fun saveActiveCallState() {
        val params = lastCallParams ?: return
        activeCallStore.save(
            ActiveCallState(
                dialogId = params.dialogId,
                contactName = params.contactName,
                avatarUrl = params.avatarUrl,
                callTitle = params.callTitle,
                isVideoCall = params.isVideoCall,
                isGroupCall = params.isGroupCall,
            )
        )
    }

    // --- Настройки звонка (шторка): аудио-устройство, камера, рука, permits ---

    /** Обновляет список аудио-выходов и текущий выбранный в состоянии звонка. */
    private fun refreshAudioRoutes() {
        val routes = callAudioManager.availableRoutes()
        val currentId = callAudioManager.currentRouteId()
        val currentName = routes.firstOrNull { it.id == currentId }?.name
        _uiState.value = _uiState.value.copy(
            audioRoutes = routes,
            currentAudioRouteId = currentId,
            currentAudioRouteName = currentName,
            currentCameraName = _uiState.value.currentCameraName ?: cameraLabel(isFrontCamera),
        )
    }

    /** Пользователь выбрал устройство вывода в листе «Выбор устройства». */
    fun selectAudioRoute(routeId: String) {
        logCallStep("audio_route_selected", "routeId=$routeId")
        callAudioManager.selectRoute(routeId)
        refreshAudioRoutes()
    }

    /**
     * Перечисляет доступные камеры (фронтальная/основная/внешние-BT) через
     * WebRTC CameraEnumerator из Flashphoner. Показывается в листе выбора камеры.
     */
    private fun refreshCameras() {
        val cameras = runCatching {
            val enumerator = com.flashphoner.fpwcsapi.Flashphoner.getCameraEnumerator()
            val names = enumerator.deviceNames ?: emptyArray()
            var frontIdx = 0
            var backIdx = 0
            names.map { name ->
                val front = runCatching { enumerator.isFrontFacing(name) }.getOrDefault(false)
                val back = runCatching { enumerator.isBackFacing(name) }.getOrDefault(false)
                val label = when {
                    front -> if (++frontIdx == 1) "Фронтальная камера" else "Фронтальная камера $frontIdx"
                    back -> if (++backIdx == 1) "Основная камера" else "Основная камера $backIdx"
                    else -> name
                }
                CameraDevice(id = name, name = label, isFront = front)
            }
        }.onFailure { logCallStep("camera_enumerate_failed", "error=${it.message}") }
            .getOrDefault(emptyList())
        val currentId = _uiState.value.currentCameraId
            ?: cameras.firstOrNull { it.isFront == isFrontCamera }?.id
            ?: cameras.firstOrNull()?.id
        val currentName = cameras.firstOrNull { it.id == currentId }?.name
            ?: cameraLabel(isFrontCamera)
        _uiState.value = _uiState.value.copy(
            availableCameras = cameras,
            currentCameraId = currentId,
            currentCameraName = _uiState.value.currentCameraName ?: currentName,
        )
        logCallStep("cameras_refreshed", "count=${cameras.size} current=$currentId")
    }

    /**
     * Выбор камеры из листа. Stream.switchCamera умеет только фронт↔тыл, поэтому
     * если выбранная камера другой ориентации — переключаем. Для той же
     * ориентации (тот же/другой девайс той же стороны) просто отмечаем выбор.
     */
    fun selectCamera(cameraId: String) {
        val target = _uiState.value.availableCameras.firstOrNull { it.id == cameraId } ?: return
        logCallStep("camera_select", "id=$cameraId isFront=${target.isFront} currentFront=$isFrontCamera")
        // Оптимистично отмечаем выбор в UI сразу (галочка/подпись меняются),
        // даже если физического переключения не будет (видео не опубликовано).
        _uiState.value = _uiState.value.copy(
            currentCameraId = cameraId,
            currentCameraName = target.name,
        )
        // Уже на этой стороне — переключать нечего.
        if (target.isFront == isFrontCamera) return
        // Нет опубликованного видео-потока — переключать физически нечего
        // (камера появится при включении видео). Выбор уже отмечен выше.
        val stream = localStream
        if (stream == null || !publishedWithVideo) {
            isFrontCamera = target.isFront
            return
        }
        runCatching {
            stream.switchCamera(object : CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    this@CallViewModel.isFrontCamera = isFrontCamera
                    val dev = _uiState.value.availableCameras.firstOrNull { it.isFront == isFrontCamera }
                    _uiState.value = _uiState.value.copy(
                        currentCameraId = dev?.id ?: cameraId,
                        currentCameraName = dev?.name ?: cameraLabel(isFrontCamera),
                    )
                    logCallStep("camera_switched", "isFront=$isFrontCamera")
                }

                override fun onCameraSwitchError(error: String?) {
                    logCallStep("camera_switch_failed", "error=$error")
                }
            })
        }.onFailure {
            logCallStep("camera_switch_exception", "error=${it.message}")
        }
    }

    /** Переключает фронтальную/основную камеру (быстрый тоггл, если понадобится). */
    fun switchCamera() {
        val stream = localStream ?: return
        runCatching {
            stream.switchCamera(object : CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    this@CallViewModel.isFrontCamera = isFrontCamera
                    val dev = _uiState.value.availableCameras.firstOrNull { it.isFront == isFrontCamera }
                    _uiState.value = _uiState.value.copy(
                        currentCameraId = dev?.id ?: _uiState.value.currentCameraId,
                        currentCameraName = dev?.name ?: cameraLabel(isFrontCamera),
                    )
                    logCallStep("camera_switched", "isFront=$isFrontCamera")
                }

                override fun onCameraSwitchError(error: String?) {
                    logCallStep("camera_switch_failed", "error=$error")
                }
            })
        }.onFailure {
            logCallStep("camera_switch_exception", "error=${it.message}")
        }
    }

    private fun cameraLabel(front: Boolean): String =
        if (front) "Фронтальная камера" else "Основная камера"

    /**
     * ВРЕМЕННО для тестов ([PREFER_BACK_CAMERA]): после публикации видео, если мы
     * на фронталке — переключаемся на заднюю камеру (тестировщика не видно).
     * Небольшая задержка — дать камере запуститься перед switchCamera.
     */
    private fun applyPreferredCamera() {
        if (!PREFER_BACK_CAMERA || !isFrontCamera) return
        viewModelScope.launch {
            delay(700)
            val stream = localStream ?: return@launch
            if (!isFrontCamera) return@launch
            runCatching {
                stream.switchCamera(object : CameraSwitchHandler {
                    override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                        this@CallViewModel.isFrontCamera = isFrontCamera
                        val dev = _uiState.value.availableCameras.firstOrNull { it.isFront == isFrontCamera }
                        _uiState.value = _uiState.value.copy(
                            currentCameraId = dev?.id ?: _uiState.value.currentCameraId,
                            currentCameraName = dev?.name ?: cameraLabel(isFrontCamera),
                        )
                        logCallStep("preferred_camera_applied", "isFront=$isFrontCamera")
                    }

                    override fun onCameraSwitchError(error: String?) {
                        logCallStep("preferred_camera_failed", "error=$error")
                    }
                })
            }.onFailure { logCallStep("preferred_camera_exception", "error=${it.message}") }
        }
    }

    /** Поднять/опустить руку — рассылается всем через updateState (cType 2006). */
    fun toggleHandRaise() {
        val current = _uiState.value.sessionState
        val updated = current.copy(handUp = !current.handUp)
        _uiState.value = _uiState.value.copy(sessionState = updated)
        updateCallState(updated)
        logCallStep("hand_raise_toggled", "handUp=${updated.handUp}")
    }

    /** Регулировка громкости звонка (STREAM_VOICE_CALL) из листа участника. */
    fun setCallVolume(fraction: Float) {
        callAudioManager.setCallVolume(fraction)
    }

    fun currentCallVolume(): Float = callAudioManager.getCallVolume()

    /**
     * Выдаёт/забирает разрешение участнику (лист «Что может участник»).
     * Доступно только администраторам/организатору; бэк вернёт 403 иначе.
     */
    fun setParticipantPermit(userId: String, permit: String, grant: Boolean) {
        val dialogId = _uiState.value.dialogId ?: return
        viewModelScope.launch {
            runCatching {
                callRepository.updatePermits(
                    dialogId = dialogId,
                    userId = userId,
                    addPermits = if (grant) listOf(permit) else null,
                    delPermits = if (grant) null else listOf(permit),
                )
            }.onSuccess {
                logCallStep("participant_permit_set", "userId=$userId permit=$permit grant=$grant")
                refreshParticipants()
            }.onFailure { error ->
                logCallStep("participant_permit_failed", "userId=$userId permit=$permit error=${error.message}")
                _uiState.value = _uiState.value.copy(
                    toastMessage = toastMessageForApiError(error),
                )
            }
        }
    }

    // --- Управление участниками (экран «Участники звонка», для администраторов) ---

    /** Впускает участника из комнаты ожидания (status 6 → 5) через REST. */
    fun allowParticipant(userId: String) {
        val dialogId = _uiState.value.dialogId ?: return
        viewModelScope.launch {
            runCatching { callRepository.updateStatus(dialogId, userId, STATUS_PARTICIPATING) }
                .onSuccess {
                    logCallStep("lobby_allow", "userId=$userId")
                    refreshParticipants()
                }
                .onFailure { error ->
                    logCallStep("lobby_allow_failed", "userId=$userId error=${error.message}")
                    _uiState.value = _uiState.value.copy(toastMessage = toastMessageForApiError(error))
                }
        }
    }

    /** «Разрешить всем» — впускает всех, кто сейчас в комнате ожидания. */
    fun allowAllFromLobby() {
        val dialogId = _uiState.value.dialogId ?: return
        val lobby = _uiState.value.lobbyParticipants.map { it.id }
        if (lobby.isEmpty()) return
        viewModelScope.launch {
            lobby.forEach { userId ->
                runCatching { callRepository.updateStatus(dialogId, userId, STATUS_PARTICIPATING) }
                    .onFailure { logCallStep("lobby_allow_all_failed", "userId=$userId error=${it.message}") }
            }
            logCallStep("lobby_allow_all", "count=${lobby.size}")
            refreshParticipants()
        }
    }

    /** Исключает участника из звонка (status → 7). */
    fun kickParticipant(userId: String) {
        val dialogId = _uiState.value.dialogId ?: return
        viewModelScope.launch {
            runCatching { callRepository.updateStatus(dialogId, userId, STATUS_KICKED) }
                .onSuccess {
                    logCallStep("participant_kicked", "userId=$userId")
                    refreshParticipants()
                }
                .onFailure { error ->
                    logCallStep("participant_kick_failed", "userId=$userId error=${error.message}")
                    _uiState.value = _uiState.value.copy(toastMessage = toastMessageForApiError(error))
                }
        }
    }

    /** Назначает участника администратором звонка (роль 37:302). Только организатор. */
    fun assignAdmin(userId: String) {
        val dialogId = _uiState.value.dialogId ?: return
        viewModelScope.launch {
            runCatching { callRepository.updatePermits(dialogId, userId, role = ROLE_ADMIN) }
                .onSuccess {
                    logCallStep("assign_admin", "userId=$userId")
                    _uiState.value = _uiState.value.copy(toastMessage = "Участник назначен администратором")
                    refreshParticipants()
                }
                .onFailure { error ->
                    logCallStep("assign_admin_failed", "userId=$userId error=${error.message}")
                    _uiState.value = _uiState.value.copy(toastMessage = toastMessageForApiError(error))
                }
        }
    }

    /** Запрещает/разрешает участнику поднимать руку (permit 82:604). */
    fun setParticipantHandRaiseAllowed(userId: String, allowed: Boolean) {
        setParticipantPermit(userId, PERMIT_RAISE_HAND, allowed)
    }

    /**
     * Принудительно выключает микрофон/камеру участника: тянет свежий
     * mediaSessionId участника и патчит его состояние. Требует прав
     * администратора (иначе бэк вернёт 403 — показываем toast). Best-effort.
     */
    private fun forceParticipantMedia(userId: String, micOff: Boolean, camOff: Boolean) {
        val dialogId = _uiState.value.dialogId ?: return
        viewModelScope.launch {
            runCatching {
                val participant = callRepository.getParticipants(dialogId).find { it.userId == userId }
                    ?: return@runCatching
                val latest = participant.states.lastOrNull() ?: return@runCatching
                val current = latest.state ?: uddug.com.domain.entities.call.CallSessionState()
                callRepository.updateState(
                    dialogId = dialogId,
                    userId = userId,
                    mediaSessionId = latest.mediaSessionId,
                    state = current.copy(
                        micOn = if (micOff) false else current.micOn,
                        camOn = if (camOff) false else current.camOn,
                    ),
                )
            }.onSuccess {
                logCallStep("participant_media_forced", "userId=$userId micOff=$micOff camOff=$camOff")
                refreshParticipants()
            }.onFailure { error ->
                logCallStep("participant_media_force_failed", "userId=$userId error=${error.message}")
                _uiState.value = _uiState.value.copy(toastMessage = toastMessageForApiError(error))
            }
        }
    }

    fun muteParticipantMic(userId: String) = forceParticipantMedia(userId, micOff = true, camOff = false)
    fun disableParticipantCamera(userId: String) = forceParticipantMedia(userId, micOff = false, camOff = true)

    /** Массово выключает микрофоны у всех активных участников. */
    fun muteAllMics() {
        _uiState.value.rosterParticipants
            .filter { it.id != profileUserId && !it.isMuted }
            .forEach { forceParticipantMedia(it.id, micOff = true, camOff = false) }
    }

    /** Массово выключает камеры у всех активных участников. */
    fun disableAllCameras() {
        _uiState.value.rosterParticipants
            .filter { it.id != profileUserId && it.camOn }
            .forEach { forceParticipantMedia(it.id, micOff = false, camOff = true) }
    }

    /** Массово запрещает поднимать руку всем активным участникам. */
    fun forbidAllRaiseHand() {
        _uiState.value.rosterParticipants
            .filter { it.id != profileUserId }
            .forEach { setParticipantHandRaiseAllowed(it.id, allowed = false) }
    }

    /**
     * Applies a mic toggle by muting/unmuting the audio track of the already
     * published local stream.
     *
     * A toggle used to do a full unpublish→republish (see [restartLocalStream]).
     * Tearing the media session down — even for the ~500 ms republish window —
     * makes the WCS server treat the user as having left, which ends a 1-to-1
     * call: the server broadcasts cType=6 and every client navigates away.
     * Muting the track keeps the session alive, so the call survives the toggle.
     */
    private fun applyLocalAudioState(micOn: Boolean) {
        val stream = localStream ?: return
        val callStatus = _uiState.value.status
        if (callStatus != CallStatus.CONNECTING && callStatus != CallStatus.IN_CALL) return
        runCatching {
            if (micOn) stream.unmuteAudio() else stream.muteAudio()
        }.onFailure {
            logCallStep("toggle_microphone_failed", "micOn=$micOn error=${it.message}")
        }
    }

    /** Camera counterpart of [applyLocalAudioState]; see its docs for rationale. */
    private fun applyLocalVideoState(camOn: Boolean) {
        val stream = localStream ?: return
        val callStatus = _uiState.value.status
        if (callStatus != CallStatus.CONNECTING && callStatus != CallStatus.IN_CALL) return
        runCatching {
            if (camOn) stream.unmuteVideo() else stream.muteVideo()
        }.onFailure {
            logCallStep("toggle_camera_failed", "camOn=$camOn error=${it.message}")
        }
    }

    fun muteParticipant(participantId: String) {
        val dialogId = _uiState.value.dialogId ?: return
        val participant = _uiState.value.participants.find { it.id == participantId } ?: return
        val newMicOn = participant.isMuted // if muted -> unmute, if not muted -> mute

        viewModelScope.launch {
            runCatching {
                callRepository.updateState(
                    dialogId = dialogId,
                    userId = participantId,
                    mediaSessionId = "$participantId#$dialogId",
                    state = CallSessionState(micOn = newMicOn, camOn = false),
                )
            }.onSuccess {
                val updatedParticipants = _uiState.value.participants.map {
                    if (it.id == participantId) it.copy(isMuted = !participant.isMuted) else it
                }
                val actionLabel = if (!newMicOn) "отключён" else "включён"
                _uiState.value = _uiState.value.copy(
                    participants = updatedParticipants,
                    toastMessage = "Микрофон ${participant.name ?: participantId} $actionLabel",
                )
                logCallStep("mute_participant", "userId=$participantId micOn=$newMicOn")
            }.onFailure { error ->
                logCallStep("mute_participant_failed", "userId=$participantId error=${error.message}")
                _uiState.value = _uiState.value.copy(
                    toastMessage = toastMessageForApiError(error),
                )
            }
        }
    }

    fun consumeToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    /**
     * Реакция на нажатие кнопки записи в шапке звонка.
     *
     * Запуск записи теперь идёт через отдельный экран настройки
     * ([startRecording] с названием файла), поэтому здесь обрабатывается только
     * остановка уже идущей записи. Если записи нет, а permit 82:608 отсутствует —
     * показываем подсказку: до экрана настройки пользователя пускать незачем.
     */
    fun toggleRecording() {
        val dialogId = _uiState.value.dialogId ?: return
        if (_uiState.value.status != CallStatus.IN_CALL) return

        logCallStep(
            "record_toggle_requested",
            "dialogId=$dialogId isRecording=${_uiState.value.isRecording} " +
                "canRecordCall=${_uiState.value.canRecordCall}"
        )

        if (!_uiState.value.isRecording) {
            if (!_uiState.value.canRecordCall) {
                logCallStep("record_permit_denied", "dialogId=$dialogId permit=$PERMIT_RECORD_CALL")
                _uiState.value = _uiState.value.copy(
                    toastMessage = "Нет разрешения на запись звонка. Обратитесь к администратору звонка",
                )
            }
            return
        }

        viewModelScope.launch {
            runCatching { callRepository.stopRecording(dialogId) }
                .onSuccess {
                    logCallStep("stop_recording_ok", "dialogId=$dialogId")
                    _uiState.value = _uiState.value.copy(isRecording = false)
                }
                .onFailure { error ->
                    logCallStep("stop_recording_failed", describeHttpError(error))
                    _uiState.value = _uiState.value.copy(
                        toastMessage = toastMessageForApiError(error),
                    )
                }
        }
    }

    /**
     * Запускает запись звонка с названием файла [fileName], выбранным
     * пользователем на экране настройки записи.
     *
     * Запускать запись можно только при наличии permit 82:608. Индикатор записи
     * у всех участников синхронизируется по сокет-событию cType 8001
     * (см. [handleRecordStatus]); здесь же выставляем флаг сразу, чтобы UI
     * отреагировал, не дожидаясь рассылки.
     */
    fun startRecording(fileName: String) {
        val dialogId = _uiState.value.dialogId ?: return
        if (_uiState.value.status != CallStatus.IN_CALL) return
        if (_uiState.value.isRecording) return

        if (!_uiState.value.canRecordCall) {
            logCallStep("record_permit_denied", "dialogId=$dialogId permit=$PERMIT_RECORD_CALL")
            _uiState.value = _uiState.value.copy(
                toastMessage = "Нет разрешения на запись звонка. Обратитесь к администратору звонка",
            )
            return
        }

        val recordName = fileName.trim()
        logCallStep("record_start_requested", "dialogId=$dialogId name=$recordName")

        viewModelScope.launch {
            runCatching { callRepository.startRecording(dialogId, recordName) }
                .onSuccess {
                    logCallStep("start_recording_ok", "dialogId=$dialogId")
                    _uiState.value = _uiState.value.copy(isRecording = true)
                }
                .onFailure { error ->
                    logCallStep("start_recording_failed", describeHttpError(error))
                    _uiState.value = _uiState.value.copy(
                        toastMessage = toastMessageForApiError(error),
                    )
                }
        }
    }

    private fun updateCallState(newState: CallSessionState) {
        val dialogId = _uiState.value.dialogId ?: return
        val sessionId = mediaSessionId ?: return
        val userId = resolveUsername()

        viewModelScope.launch {
            runCatching {
                callRepository.updateState(
                    dialogId = dialogId,
                    userId = userId,
                    mediaSessionId = sessionId,
                    state = newState,
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(sessionState = newState)
            }.onFailure { error ->
                // The local mic/camera state is already applied via Flashphoner
                // (the stream is republished with new constraints), so the call
                // itself is unaffected. This PATCH only syncs server-side
                // bookkeeping. A failure here must NOT raise a "Недостаточно
                // прав" toast: the user does hold the mic/camera permits, the
                // toggle visibly worked, and an alarming message is misleading.
                // Just log it.
                logCallStep("update_state_failed", "error=${error.message}")
            }
        }
    }

    /**
     * Разворачивает ошибку API в строку для логов: для HTTP-ответа достаёт
     * код и тело ответа сервера. Нужно для диагностики 403 на записи звонка —
     * чтобы видеть, что именно вернул бэк (нет permit / не в диалоге / etc).
     */
    private fun describeHttpError(error: Throwable): String {
        val http = error as? HttpException
            ?: return "type=${error.javaClass.simpleName} message=${error.message}"
        val body = runCatching { http.response()?.errorBody()?.string() }.getOrNull()
        return "httpCode=${http.code()} message=${http.message()} body=${body ?: "n/a"}"
    }

    private fun toastMessageForApiError(error: Throwable): String {
        val message = error.message.orEmpty()
        val looksForbidden = message.contains("403") ||
            message.contains("405") ||
            message.contains("401") ||
            message.contains("Not Allowed", ignoreCase = true) ||
            message.contains("Forbidden", ignoreCase = true)
        return if (looksForbidden) {
            "Недостаточно прав для этого действия"
        } else {
            "Не удалось обновить состояние звонка"
        }
    }

    private fun subscribeToParticipants(participants: Collection<Participant>) {
        participants.forEach { participant ->
            if (isSelfParticipant(participant)) {
                logCallStep("participant_self_ignored", "participant=${participant.name}")
                return@forEach
            }
            val participantId = participant.name ?: return@forEach
            participantHandles[participantId] = participant
            if (findStreamForParticipant(participantId) != null) return@forEach
            val renderer = rendererForParticipant(participantId)
            // Video call: defer play() until a UI renderer exists — a stream
            // played onto a null renderer stays black until switchRenderer().
            // Audio call: there is no video surface at all, so play right away,
            // otherwise the call would hang on "Подключаемся" forever.
            if (renderer != null || !_uiState.value.isVideoCall) {
                playParticipant(participant, participantId, renderer)
            }
        }
    }

    /**
     * Resolves the renderer a remote participant should be played onto.
     * Group call — the per-tile renderer bound via [bindParticipantRenderer].
     * 1-to-1 call — the shared main surface bound via [bindRemoteRenderer].
     */
    private fun rendererForParticipant(participantId: String): SurfaceViewRenderer? {
        participantRenderers[participantId]?.let { return it }
        return if (_uiState.value.isGroupCall) null else remoteRenderer
    }

    /**
     * (Re)subscribes to a participant's remote stream and binds it to [renderer].
     * Any previous stream for the participant is stopped first: re-attaching a
     * released renderer to an existing stream via switchRenderer() does not
     * reliably restore video (black tile after minimizing or re-entering the
     * call screen). A fresh play() with the live renderer renders correctly —
     * the same path a newly joined participant takes.
     */
    private fun playParticipant(
        participant: Participant,
        participantId: String,
        renderer: SurfaceViewRenderer?,
    ) {
        detachParticipantStream(participantId)
        val stream = runCatching { participant.play(renderer) }
            .onFailure {
                logCallStep("remote_play_failed", "participant=$participantId error=${it.message}")
            }
            .getOrNull() ?: return
        val key = participant.streamName ?: participantId
        participantStreams[key] = stream
        if (primaryStreamKey == null) {
            primaryStreamKey = key
            remoteStream = stream
        }
        attachStreamDiagnostics(stream, "remote_play:$key")
        markRemoteParticipantConnected()
        remoteResubscribeAttempts.remove(participantId)
        logCallStep("remote_play_started", "participant=$participantId key=$key")
    }

    /** Stops and forgets any remote stream(s) currently held for [participantId]. */
    private fun detachParticipantStream(participantId: String) {
        val keys = participantStreams.keys.filter {
            it == participantId || it.startsWith("$participantId#")
        }
        keys.forEach { key ->
            participantStreams.remove(key)?.let { stream ->
                runCatching { stream.switchRenderer(null) }
                runCatching { stream.stop() }
            }
            if (primaryStreamKey == key) {
                primaryStreamKey = null
                remoteStream = null
            }
        }
    }

    /**
     * Forces a fresh subscription for a participant that has just published.
     * A stream opened in onState()/onJoined() before the participant actually
     * published never receives media, so it is replaced once onPublished()
     * confirms media is flowing.
     */
    private fun forceResubscribeParticipant(participant: Participant) {
        val participantId = participant.name ?: return
        participantHandles[participantId] = participant
        val renderer = rendererForParticipant(participantId)
        // Video: wait for a renderer. Audio: play immediately (no surface).
        if (renderer != null || !_uiState.value.isVideoCall) {
            playParticipant(participant, participantId, renderer)
        } else {
            // Tile not composed yet — drop any stale stream so the eventual
            // bindParticipantRenderer() triggers a fresh play().
            detachParticipantStream(participantId)
        }
    }

    private fun addParticipantToUiState(participant: Participant) {
        val participantId = participant.name ?: return
        val current = _uiState.value.participants
        if (current.any { it.id == participantId }) return
        val cached = current.associateBy { it.id }
        val resolvedName = cached[participantId]?.name ?: participantId
        _uiState.value = _uiState.value.copy(
            participants = current + CallParticipant(
                id = participantId,
                name = resolvedName,
                avatarUrl = cached[participantId]?.avatarUrl,
            )
        )
        if (resolvedName == participantId) {
            refreshParticipants()
        }
    }

    private fun removeParticipantStream(participant: Participant) {
        val key = participant.streamName ?: participant.name ?: return
        val removed = participantStreams.remove(key) ?: return
        runCatching { removed.switchRenderer(null) }
        if (remoteStream == removed) {
            remoteStream = null
        }
        if (primaryStreamKey == key) {
            primaryStreamKey = participantStreams.keys.firstOrNull()
            val nextStream = primaryStreamKey?.let { participantStreams[it] }
            if (remoteRenderer != null && nextStream != null) {
                runCatching { nextStream.switchRenderer(remoteRenderer) }
            }
        }
        runCatching { removed.stop() }
    }

    private fun clearVideoStreams() {
        // Crash-safety (ресерч WCS/WebRTC): getStats/renderer нельзя дёргать по
        // потоку, который останавливается — иначе нативный SIGABRT
        // (!env->ExceptionCheck) на signaling-треде. Гасим VU-метр и запланированные
        // пере-подписки/переиздания ДО stop() потоков.
        stopVuMeter()
        localRepublishJob?.cancel()
        remoteResubscribeJobs.values.forEach { it.cancel() }

        val streamsToStop = LinkedHashSet<Stream>().apply {
            addAll(participantStreams.values)
            localStream?.let(::add)
            remoteStream?.let(::add)
        }

        // Сначала снимаем рендерер со ВСЕХ потоков, только потом останавливаем —
        // не переиспользуем рендерер, который ещё в середине teardown.
        streamsToStop.forEach { runCatching { it.switchRenderer(null) } }
        streamsToStop.forEach { runCatching { it.stop() } }

        participantStreams.clear()
        participantRenderers.clear()
        participantHandles.clear()
        remoteResubscribeJobs.values.forEach { it.cancel() }
        remoteResubscribeJobs.clear()
        remoteResubscribeAttempts.clear()
        primaryStreamKey = null
        localStream = null
        remoteStream = null
        pendingRemoteParticipant = null
        localRenderer = null
        remoteRenderer = null
    }

    private fun handleCallFailure(@Suppress("UNUSED_PARAMETER") message: String? = null) {
        logCallStep("call_failed", message ?: "unknown")
        incomingCallStore.clear()
        activeCallStore.clear()
        lastCallParams = null
        clearVideoStreams()
        flashphonerSessionManager.disconnectRoom()
        callAudioManager.release()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
            errorMessage = message,
        )
        stopCallTimer()
        isCallStarted = false
        stopParticipantWatchdog()
        localPublishStarted = false
        publishedWithVideo = false
        callOperationId = null
        currentConfig = null
        currentRoomName = null
        currentStreamName = null
        currentLogin = null
        profileUserId = null
        clearVideoState()
    }

    private fun logWcsDiagnostics(
        selectedLogin: String,
        serverUrl: String,
        streamName: String,
        dialogId: Long,
    ) {
        Log.d(
            LOG_TAG,
            "wcs_diagnostics selectedLogin=$selectedLogin source=profileApi profileUserId=$profileUserId"
        )
        Log.d(
            LOG_TAG,
            "wcs_diagnostics connection serverUrl=$serverUrl dialogId=$dialogId streamName=$streamName"
        )
    }

    private suspend fun resolveWcsLogin(): String {
        val profile = withContext(Dispatchers.IO) { userProfileRepository.getProfileInfo().await() }
        val id = profile.id
        profileUserId = id
        _uiState.value = _uiState.value.copy(
            currentUserId = id,
            currentUserAvatarUrl = profile.image?.path,
        )
        return normalizeWcsLogin(id)
            ?: error("User ID from profile API is null or invalid: '$id'")
    }

    private fun normalizeWcsLogin(raw: String?): String? {
        val normalized = raw?.trim().orEmpty()
        if (normalized.isBlank()) {
            Log.d(LOG_TAG, "wcs_identity_reject reason=blank raw=$raw")
            return null
        }
        if (normalized.equals("anonymous", ignoreCase = true)) {
            Log.d(LOG_TAG, "wcs_identity_reject reason=anonymous raw=$raw")
            return null
        }
        if (normalized.equals("null", ignoreCase = true)) {
            Log.d(LOG_TAG, "wcs_identity_reject reason=literal_null raw=$raw")
            return null
        }
        return normalized
    }

    private fun resolveUsername(): String {
        return normalizeWcsLogin(currentLogin)
            ?: normalizeWcsLogin(profileUserId)
            ?: "unknown"
    }

    private data class CallParams(
        val dialogId: Long,
        val contactName: String?,
        val avatarUrl: String?,
        val participants: List<CallParticipant>?,
        val callTitle: String?,
        val isVideoCall: Boolean,
        val isGroupCall: Boolean = false,
    )

    private companion object {
        const val MAX_RECONNECT_ATTEMPTS = 3
        const val RECONNECT_BASE_DELAY_MS = 1_500L
        // Пере-подписка на упавший чужой поток. Flashphoner рекомендует 3с и одну
        // попытку в полёте (агрессивные ~1.5с складывают полу-снесённые
        // PeerConnection'ы → нестабильность, нативный SIGABRT и в итоге сервер
        // завершает звонок). Экспоненциальный backoff, потолок задержки и попыток.
        const val MAX_REMOTE_RESUBSCRIBE = 8
        const val REMOTE_RESUBSCRIBE_DELAY_MS = 3_000L
        const val REMOTE_RESUBSCRIBE_MAX_DELAY_MS = 15_000L
        // Republish локального потока после FAILED: несколько попыток с backoff,
        // чтобы пережить «призрачную» сессию с тем же именем (RoomApi публикует
        // под фиксированным roomId-login, так что уникальным именем не обойти —
        // ждём, пока сервер снимет старую по keep-alive). См. ресерч по WCS.
        // ТОЛЬКО одна отложенная попытка, без цикла. Каждый republish
        // (unpublish→publish) заново открывает захват микрофона; частый churn
        // роняет аудио-HAL эмулятора («Assertion failed: !mSource» в mic
        // ReadThread) и/или WS-сессию комнаты → весь звонок падает. Дадим призраку
        // сессии протухнуть по keep-alive и попробуем один раз аудио-only.
        const val LOCAL_REPUBLISH_MAX_ATTEMPTS = 1
        // Коллизия имени/сессии со старой сессией (реджойн): сервер освободит имя,
        // поэтому ждём и повторяем несколько раз с backoff.
        const val LOCAL_REPUBLISH_MAX_ATTEMPTS_COLLISION = 5
        const val LOCAL_REPUBLISH_BASE_DELAY_MS = 6_000L
        const val PARTICIPANT_WAIT_TIMEOUT_MS = 10_000L
        const val LOG_TAG = "CallFlow"
        const val ROLE_ORGANIZER = "37:301"
        const val ROLE_ADMIN = "37:302"
        const val PERMIT_MANAGE_PARTICIPANTS = "82:611"
        const val PERMIT_ASSIGN_ADMIN = "82:610"
        const val PERMIT_RAISE_HAND = "82:604"
        const val PERMIT_RECORD_CALL = "82:608"
        const val STATUS_PARTICIPATING = 5
        const val STATUS_LOBBY = 6
        const val STATUS_KICKED = 7
        const val CTYPE_STATE_CHANGED = 2006
        const val CTYPE_PERMITS_CHANGED = 2007
        const val CTYPE_JOIN_CALL = 2002
        const val CTYPE_LET_JOIN = 2003
        const val CTYPE_CALL_USERS = 2004
        const val CTYPE_CALL_PARTICIPATE = 2005
        const val CTYPE_STATUS_CHANGED = 2008
        const val CTYPE_CALL_DECLINED = 6001
        // cType 8001 — уведомление об изменении статуса записи звонка.
        const val CTYPE_RECORD_STATUS = 8001
        // Статусы записи (справочник #179):
        // 1 — Инициализация, 2 — Активная, 3 — Завершена, 4 — Ошибка.
        const val RECORD_STATUS_INITIALIZING = 1
        const val RECORD_STATUS_ACTIVE = 2
        const val RECORD_STATUS_FINISHED = 3
        const val RECORD_STATUS_ERROR = 4
        const val SOCKET_LISTENER_TAG = "CallViewModel"
        const val STREAM_RESTART_DELAY_MS = 500L
        // ВРЕМЕННО для тестов: публиковать заднюю камеру по умолчанию (чтобы
        // тестировщика не было видно). Позже вернуть в false → будет фронталка.
        const val PREFER_BACK_CAMERA = true
        // VU-метр: период опроса и порог «говорит» по WebRTC audioLevel (0..1).
        const val VU_METER_INTERVAL_MS = 200L
        const val SPEAKING_THRESHOLD = 0.06
    }

    private fun buildStreamName(dialogId: Long, username: String): String {
        return "$username#$dialogId"
    }

    override fun onCleared() {
        socketService.removeEvent("message", SOCKET_LISTENER_TAG)
        callAudioManager.onRoutesChanged = null
        callAudioManager.release()
        clearVideoStreams()
        flashphonerSessionManager.reset()
        super.onCleared()
    }

    fun bindLocalRenderer(renderer: SurfaceViewRenderer) {
        localRenderer = renderer
        // switchRenderer() кидает NPE, если у стрима ещё/уже нет живого
        // MediaConnection (не опубликован после аудио→видео републиша или
        // реконнекта) — крашило при включении камеры. Гасим.
        runCatching { localStream?.switchRenderer(renderer) }
            .onFailure { logCallStep("bind_local_renderer_failed", "error=${it.message}") }
    }

    fun bindRemoteRenderer(renderer: SurfaceViewRenderer) {
        remoteRenderer = renderer
        // 1-to-1 call: play the lone remote participant onto the main surface
        // as soon as it is composed. A fresh play() also restores video when
        // the call screen is re-entered or expanded from the overlay.
        val handle = participantHandles.values.firstOrNull()
        if (handle != null) {
            val participantId = handle.name
            if (participantId != null) {
                playParticipant(handle, participantId, renderer)
                return
            }
        }
        runCatching { remoteStream?.switchRenderer(renderer) }
        if (remoteStream == null) {
            pendingRemoteParticipant?.let { participant ->
                remoteStream = runCatching { participant.play(renderer) }.getOrNull()
            }
            pendingRemoteParticipant = null
        }
    }

    fun clearRenderers() {
        localRenderer = null
        remoteRenderer = null
    }

    fun clearLocalRenderer() {
        runCatching { localStream?.switchRenderer(null) }
        localRenderer = null
    }

    fun clearRemoteRenderer() {
        runCatching { remoteStream?.switchRenderer(null) }
        remoteRenderer = null
    }

    fun bindParticipantRenderer(participantId: String, renderer: SurfaceViewRenderer) {
        participantRenderers[participantId] = renderer
        val handle = participantHandles[participantId]
        if (handle != null) {
            // A fresh renderer was created for this tile (first composition,
            // re-entering the call screen, or expanding from the overlay).
            // Re-play onto it so video is restored — see playParticipant().
            playParticipant(handle, participantId, renderer)
        } else {
            runCatching { findStreamForParticipant(participantId)?.switchRenderer(renderer) }
        }
    }

    fun releaseParticipantRenderer(participantId: String) {
        participantRenderers.remove(participantId)
        runCatching { findStreamForParticipant(participantId)?.switchRenderer(null) }
    }

    private fun findStreamForParticipant(participantId: String): Stream? {
        val key = participantStreams.keys.find { k ->
            k == participantId || k.startsWith("$participantId#")
        }
        return key?.let { participantStreams[it] }
    }

    private fun clearVideoState() {
        localStream = null
        remoteStream = null
        pendingRemoteParticipant = null
        clearRenderers()
    }

    private fun isSelfParticipant(participant: Participant): Boolean {
        val username = currentLogin ?: return false
        if (participant.name == username) return true

        val expectedStreamName = currentStreamName
        if (!expectedStreamName.isNullOrBlank() && participant.streamName == expectedStreamName) {
            return true
        }

        return participant.streamName?.contains(username) == true
    }

    private fun startParticipantWatchdog() {
        participantWatchdogJob?.cancel()
        participantWatchdogJob = viewModelScope.launch {
            while (isActive && lastParticipantsCount == 0 && isCallStarted) {
                delay(PARTICIPANT_WAIT_TIMEOUT_MS)
                if (lastParticipantsCount == 0 && isCallStarted) {
                    logDiagnostics("participant_watchdog_timeout")
                    logCallStep("participant_wait_retry", "timeoutMs=$PARTICIPANT_WAIT_TIMEOUT_MS")
                }
            }
        }
    }

    private fun stopParticipantWatchdog() {
        participantWatchdogJob?.cancel()
        participantWatchdogJob = null
    }

    private fun checkParticipantWatchdog() {
        if (lastParticipantsCount > 0) {
            stopParticipantWatchdog()
        }
    }

    private fun markRemoteParticipantConnected() {
        if (_uiState.value.status != CallStatus.IN_CALL) {
            _uiState.value = _uiState.value.copy(status = CallStatus.IN_CALL)
            startCallTimer()
            refreshParticipants()
        }
    }

    private fun handleSocketMessage(message: Any) {
        val jsonString = when (message) {
            is String -> message
            is JSONObject -> message.toString()
            else -> return
        }
        val json = runCatching { JSONObject(jsonString) }.getOrNull() ?: return
        val cType = json.optInt("cType", 0)
        val dialogId = json.optLong("dialog", 0L)
        if (dialogId != _uiState.value.dialogId) return

        when (cType) {
            CTYPE_STATE_CHANGED -> handleStateChange(json)
            CTYPE_PERMITS_CHANGED -> handlePermitsChange(json)
            CTYPE_RECORD_STATUS -> handleRecordStatus(json)
            CTYPE_STATUS_CHANGED -> handleStatusChange(json)
            // Разрешение перехода из комнаты ожидания в звонок: администратор
            // разрешил присоединиться. Если это про нас и мы ещё не в комнате
            // WCS — подключаемся к звонку.
            CTYPE_CALL_USERS, CTYPE_CALL_PARTICIPATE, CTYPE_LET_JOIN -> {
                if (isSelfTargeted(json)) joinFromLobby()
                refreshParticipants()
            }
        }
    }

    /**
     * cType 2008 — обновление статусов участников (комната ожидания ↔ участвует,
     * исключение). Обновляем ростер; если исключили нас самих (status 7) —
     * завершаем звонок локально с уведомлением.
     */
    private fun handleStatusChange(json: JSONObject) {
        val selfId = profileUserId
        val updates = json.optJSONArray("statusUpdates")
        var selfKicked = false
        if (updates != null && selfId != null) {
            outer@ for (i in 0 until updates.length()) {
                val update = updates.optJSONObject(i) ?: continue
                if (update.optInt("status", 0) != STATUS_KICKED) continue
                val users = update.optJSONArray("users") ?: continue
                for (j in 0 until users.length()) {
                    if (users.optString(j) == selfId) { selfKicked = true; break@outer }
                }
            }
        }
        if (selfKicked) {
            logCallStep("self_kicked", "status=$STATUS_KICKED")
            _uiState.value = _uiState.value.copy(toastMessage = "Вас исключили из звонка")
            endCall()
            return
        }
        refreshParticipants()
    }

    /**
     * cType 2007 — изменение прав/ролей. Если админом назначили НАС — показываем
     * уведомление «Вас назначили администратором» (дизайн). Затем обновляем ростер.
     */
    private fun handlePermitsChange(json: JSONObject) {
        val selfId = profileUserId
        val update = json.optJSONObject("permitsUpdate")
        val user = update?.optString("user")
        val role = update?.optString("role")
        if (selfId != null && user == selfId && role == ROLE_ADMIN) {
            logCallStep("self_assigned_admin", "role=$role")
            _uiState.value = _uiState.value.copy(
                toastMessage = "Вас назначили администратором",
            )
        }
        refreshParticipants()
    }

    /** true, если событие (2003/2004/2005) адресовано текущему пользователю. */
    private fun isSelfTargeted(json: JSONObject): Boolean {
        val selfId = profileUserId ?: return false
        val users = json.optJSONArray("users") ?: return false
        for (i in 0 until users.length()) {
            if (users.optString(i) == selfId) return true
        }
        return false
    }

    /**
     * Нас выпустили из комнаты ожидания — присоединяемся к активному звонку.
     * Если WCS-сессия уже поднята, ничего не делаем; иначе переиспользуем путь
     * реконнекта (startCall с сохранёнными параметрами).
     */
    private fun joinFromLobby() {
        awaitingJoinApproval = false
        // Если мы всё ещё в WCS-комнате (после реджойна публикацию терминировали,
        // но комната не разорвана) — не перезаходим, а просто ПУБЛИКУЕМСЯ снова:
        // теперь сервер разрешил (status→5), терминации не будет.
        if (flashphonerSessionManager.isRoomJoined()) {
            logCallStep("join_from_lobby", "already in room → republish")
            localPublishStarted = false
            hasPublishedLocalStream = false
            localRepublishAttempts = 0
            val session = _uiState.value.sessionState
            restartLocalStream(
                audioEnabled = session.micOn,
                videoEnabled = _uiState.value.isVideoCall,
            )
            return
        }
        val params = lastCallParams ?: return
        logCallStep("join_from_lobby", "dialogId=${params.dialogId}")
        isCallStarted = false
        flashphonerSessionManager.reset()
        startCall(
            dialogId = params.dialogId,
            contactName = params.contactName,
            avatarUrl = params.avatarUrl,
            participants = params.participants,
            callTitle = params.callTitle,
            isVideoCall = params.isVideoCall,
            resetReconnectAttempts = true,
            isGroupCall = params.isGroupCall,
        )
    }

    /**
     * Обрабатывает уведомление о статусе записи звонка (cType 8001).
     * Платформа рассылает его всем активным участникам, поэтому индикатор
     * записи синхронизируется у всех, а не только у инициатора. Формат:
     * { "dialog": 123, "record": { "id": 12345, "status": 1 }, "cType": 8001 }
     */
    private fun handleRecordStatus(json: JSONObject) {
        val record = json.optJSONObject("record")
        val status = record?.optInt("status", 0) ?: 0
        val recordId = record?.optLong("id", 0L) ?: 0L
        logCallStep("record_status_event", "recordId=$recordId status=$status")

        when (status) {
            RECORD_STATUS_INITIALIZING, RECORD_STATUS_ACTIVE -> {
                _uiState.value = _uiState.value.copy(isRecording = true)
            }
            RECORD_STATUS_FINISHED -> {
                _uiState.value = _uiState.value.copy(isRecording = false)
            }
            RECORD_STATUS_ERROR -> {
                _uiState.value = _uiState.value.copy(
                    isRecording = false,
                    toastMessage = "Не удалось записать звонок",
                )
            }
        }
    }

    private fun handleStateChange(json: JSONObject) {
        val statesArray = json.optJSONArray("states") ?: return
        val selfId = profileUserId

        for (i in 0 until statesArray.length()) {
            val stateObj = statesArray.optJSONObject(i) ?: continue
            val userId = stateObj.optString("user")
            val stateInner = stateObj.optJSONObject("state") ?: continue
            val micOn = stateInner.optBoolean("micOn", true)

            if (userId == selfId) {
                val selfState = _uiState.value.sessionState
                val handUp = stateInner.optBoolean("handUp", selfState.handUp)
                var newSelf = selfState.copy(handUp = handUp)
                var toast: String? = null
                if (!micOn && selfState.micOn) {
                    newSelf = newSelf.copy(micOn = false)
                    toast = "Администратор отключил ваш микрофон"
                }
                // Админ опустил вашу руку (была поднята, стала опущена).
                if (!handUp && selfState.handUp) {
                    toast = "Администратор опустил вашу руку"
                }
                _uiState.value = _uiState.value.copy(
                    sessionState = newSelf,
                    toastMessage = toast ?: _uiState.value.toastMessage,
                )
            } else {
                val participant = _uiState.value.participants.find { it.id == userId }
                if (participant != null) {
                    val camOn = stateInner.optBoolean("camOn", participant.camOn)
                    val handUp = stateInner.optBoolean("handUp", participant.handUp)
                    val updatedParticipants = _uiState.value.participants.map {
                        if (it.id == userId) it.copy(isMuted = !micOn, camOn = camOn, handUp = handUp) else it
                    }
                    _uiState.value = _uiState.value.copy(participants = updatedParticipants)
                }
            }
        }
    }

    private fun refreshParticipants() {
        val dialogId = _uiState.value.dialogId ?: return

        viewModelScope.launch {
            // Имена/аватары участников берём из ростера группы
            // (getDialogInfo().users), а данные call-API (статус/роли/мьют) —
            // best-effort поверх. Два запроса независимы: падение одного не
            // должно лишать участников имён из другого источника.
            val apiParticipants = runCatching {
                callRepository.getParticipants(dialogId)
            }.onFailure {
                logCallStep("participants_api_failed", "dialogId=$dialogId error=${it.message}")
            }.getOrDefault(emptyList())

            val dialogUsers = runCatching {
                chatInteractor.getDialogInfo(dialogId).users
            }.onFailure {
                logCallStep("dialog_roster_failed", "dialogId=$dialogId error=${it.message}")
            }.getOrNull()
                ?.associateBy { it.userId }
                ?: emptyMap()

            if (apiParticipants.isEmpty() && dialogUsers.isEmpty()) {
                logCallStep("participants_refresh_empty", "dialogId=$dialogId")
                return@launch
            }

            val selfId = profileUserId
            val selfParticipant = apiParticipants.find { it.userId == selfId }
            val isOrganizer = selfParticipant?.roles?.contains(ROLE_ORGANIZER) == true
            val isAdmin = selfParticipant != null && (
                selfParticipant.roles.any { it == ROLE_ORGANIZER || it == ROLE_ADMIN } ||
                    selfParticipant.permits.contains(PERMIT_MANAGE_PARTICIPANTS)
                )
            // Право на запись звонка (permit 82:608). Если участник self не
            // найден в ответе — сохраняем прежнее значение, чтобы не блокировать.
            val canRecord = if (selfParticipant != null) {
                selfParticipant.permits.contains(PERMIT_RECORD_CALL)
            } else {
                _uiState.value.canRecordCall
            }

            val restById = apiParticipants
                .filter { it.userId != selfId }
                .associateBy { it.userId }

            // Builds a CallParticipant, resolving the display name from the
            // dialog roster first (the reliable source of human names), then
            // the call-API fullName, and only as a last resort the raw UUID.
            fun describeParticipant(
                participantId: String,
                fallback: CallParticipant?,
            ): CallParticipant {
                val rest = restById[participantId]
                val dialogUser = dialogUsers[participantId]
                val latestState = rest?.states?.lastOrNull()?.state
                return CallParticipant(
                    id = participantId,
                    name = dialogUser?.fullName?.takeIf { it.isNotBlank() }
                        ?: dialogUser?.nickname?.takeIf { it.isNotBlank() }
                        ?: rest?.fullName?.takeIf { it.isNotBlank() }
                        ?: fallback?.name?.takeIf { it.isNotBlank() && it != participantId }
                        ?: participantId,
                    avatarUrl = dialogUser?.image?.takeIf { it.isNotBlank() }
                        ?: rest?.imageUrl?.takeIf { it.isNotBlank() }
                        ?: fallback?.avatarUrl,
                    isMuted = if (rest != null) {
                        latestState?.micOn == false
                    } else {
                        fallback?.isMuted ?: false
                    },
                    camOn = latestState?.camOn ?: fallback?.camOn ?: true,
                    handUp = latestState?.handUp ?: fallback?.handUp ?: false,
                    roles = rest?.roles ?: fallback?.roles ?: emptyList(),
                    permits = rest?.permits ?: fallback?.permits ?: emptyList(),
                )
            }

            // Enrich the tiles already shown (added from Flashphoner room
            // events) with real names/avatars instead of replacing the whole
            // list — a full replace races room events and leaves UUID-named
            // black tiles. Removal stays driven by RoomEvent.onLeft.
            val currentIds = _uiState.value.participants.map { it.id }.toSet()
            val enriched = _uiState.value.participants
                .map { describeParticipant(it.id, it) }
            // Add participants the backend reports as in the call but that no
            // room event has surfaced yet.
            val missing = apiParticipants
                .filter {
                    it.userId != selfId &&
                        it.status == STATUS_PARTICIPATING &&
                        it.userId !in currentIds
                }
                .map { describeParticipant(it.userId, null) }

            // Full roster for the «Участники звонка» screen. Maps every backend
            // participant (including self) to a UI model, resolving names/avatars
            // from the dialog roster. Split into active (status 5) and waiting
            // room (status 6); other statuses are terminal and not shown.
            fun toRoster(p: uddug.com.domain.entities.call.CallParticipant): CallParticipant {
                val dialogUser = dialogUsers[p.userId]
                val latestState = p.states.lastOrNull()?.state
                return CallParticipant(
                    id = p.userId,
                    name = dialogUser?.fullName?.takeIf { it.isNotBlank() }
                        ?: dialogUser?.nickname?.takeIf { it.isNotBlank() }
                        ?: p.fullName?.takeIf { it.isNotBlank() }
                        ?: p.userId,
                    avatarUrl = dialogUser?.image?.takeIf { it.isNotBlank() }
                        ?: p.imageUrl?.takeIf { it.isNotBlank() },
                    isMuted = latestState?.micOn == false,
                    camOn = latestState?.camOn ?: true,
                    handUp = latestState?.handUp ?: false,
                    roles = p.roles,
                    permits = p.permits,
                    status = p.status,
                )
            }
            val restRoster = apiParticipants
                .filter { it.status == STATUS_PARTICIPATING }
                .map(::toRoster)
            val restRosterIds = restRoster.mapTo(HashSet()) { it.id }

            // Себя показываем ВСЕГДА: если REST не вернул участников (частый флейк
            // на активном звонке) — экран не должен быть пустым. Собираем self из
            // профиля/сессии, если его нет в ответе REST.
            val selfEntry: CallParticipant? = when {
                selfId == null -> null
                restRosterIds.contains(selfId) -> null
                else -> CallParticipant(
                    id = selfId,
                    name = dialogUsers[selfId]?.fullName?.takeIf { it.isNotBlank() }
                        ?: selfParticipant?.fullName?.takeIf { it.isNotBlank() }
                        ?: "Вы",
                    avatarUrl = _uiState.value.currentUserAvatarUrl
                        ?: dialogUsers[selfId]?.image,
                    isMuted = !_uiState.value.sessionState.micOn,
                    camOn = _uiState.value.sessionState.camOn,
                    handUp = _uiState.value.sessionState.handUp,
                    roles = selfParticipant?.roles ?: emptyList(),
                    permits = selfParticipant?.permits ?: emptyList(),
                    status = STATUS_PARTICIPATING,
                )
            }

            // Фолбэк по удалённым: если REST не отдал активных участников, но у нас
            // есть плитки (из room-событий Flashphoner) — показываем их, чтобы
            // ростер соответствовал реально идущему звонку.
            val tileFallback = enriched.filter { it.id != selfId && it.id !in restRosterIds }

            val roster = (listOfNotNull(selfEntry) + restRoster + tileFallback)
                .distinctBy { it.id }
                .sortedByDescending { it.roles.contains(ROLE_ORGANIZER) }
            val lobby = apiParticipants
                .filter { it.status == STATUS_LOBBY }
                .map(::toRoster)

            _uiState.value = _uiState.value.copy(
                participants = enriched + missing,
                isCurrentUserAdmin = isAdmin,
                isCurrentUserOrganizer = isOrganizer,
                rosterParticipants = roster,
                lobbyParticipants = lobby,
                canRecordCall = canRecord,
            )
            logCallStep(
                "participants_refreshed",
                "dialogId=$dialogId tiles=${enriched.size + missing.size} " +
                    "apiCount=${apiParticipants.size} rosterCount=${roster.size} lobbyCount=${lobby.size} " +
                    "isAdmin=$isAdmin isOrg=$isOrganizer selfInRest=${restRosterIds.contains(selfId)}"
            )
        }
    }

    private fun attachStreamDiagnostics(stream: Stream?, label: String) {
        if (stream == null) return
        stream.on { _, status ->
            // ЭТОТ колбэк выполняется на нативном WebRTC-треде через JNI. Любое
            // непойманное Java-исключение здесь = `!env->ExceptionCheck()` →
            // SIGABRT (весь процесс падает). Оборачиваем ВСЁ тело в runCatching.
            runCatching {
                val statusText = status.toString()
                // На FAILED вытаскиваем ТОЧНУЮ причину (getInfo): STREAM_NAME_ALREADY_IN_USE,
                // FAILED_BY_ICE_*, MEDIASESSION_ID_ALREADY_IN_USE и т.п. — чтобы чинить
                // прицельно, а не гадать.
                val info = if (statusText.equals("FAILED", ignoreCase = true)) {
                    runCatching { stream.getInfo() }.getOrNull()
                } else null
                logCallStep("stream_status", "label=$label status=$statusText" + (info?.let { " info=$it" } ?: ""))
                handleLocalPublishStatus(label, statusText, info)
                if (label.startsWith("remote_play:") && statusText.equals("FAILED", ignoreCase = true)) {
                    scheduleRemoteResubscribe(label.removePrefix("remote_play:"))
                }
                maybeSanitizeRemoteSdp(stream, label)
            }.onFailure {
                logCallStep("stream_status_callback_error", "label=$label error=${it.message}")
            }
        }
    }

    /**
     * A remote participant's WebRTC stream can transiently drop to FAILED on a
     * network blip (their side reconnecting, packet loss). Without recovery the
     * tile freezes forever ("I can't see/hear the others"). Re-subscribe to that
     * participant after a short delay, capped per participant so a genuinely
     * gone stream does not loop. The counter is reset on a successful play so a
     * later, unrelated blip gets a fresh budget.
     */
    private fun scheduleRemoteResubscribe(streamKey: String) {
        val participant = participantHandles.values.firstOrNull { p ->
            (p.streamName ?: p.name) == streamKey ||
                p.name?.let { streamKey.contains(it) } == true
        } ?: return
        val pid = participant.name ?: return
        val attempts = remoteResubscribeAttempts.getOrDefault(pid, 0)
        if (attempts >= MAX_REMOTE_RESUBSCRIBE) {
            logCallStep("remote_resubscribe_giveup", "participant=$pid attempts=$attempts")
            return
        }
        if (remoteResubscribeJobs[pid]?.isActive == true) return
        // Экспоненциальный backoff (3с, 6с, 9с… с потолком), одна попытка в
        // полёте — как рекомендует Flashphoner. Не долбим по 1.5с.
        val delayMs = (REMOTE_RESUBSCRIBE_DELAY_MS * (attempts + 1))
            .coerceAtMost(REMOTE_RESUBSCRIBE_MAX_DELAY_MS)
        remoteResubscribeJobs[pid] = viewModelScope.launch {
            delay(delayMs)
            if (_uiState.value.status != CallStatus.IN_CALL &&
                _uiState.value.status != CallStatus.CONNECTING
            ) return@launch
            val handle = participantHandles[pid] ?: return@launch
            remoteResubscribeAttempts[pid] = attempts + 1
            logCallStep("remote_resubscribe", "participant=$pid attempt=${attempts + 1} delayMs=$delayMs")
            forceResubscribeParticipant(handle)
        }
    }

    private fun handleLocalPublishStatus(label: String, status: String, info: String? = null) {
        if (label != "local_publish") return
        when (status.uppercase()) {
            "PUBLISHING" -> logCallStep("publish_local_stream_status_publishing", "status=$status")
            "PUBLISHED" -> {
                hasPublishedLocalStream = true
                localRepublishAttempts = 0
                awaitingJoinApproval = false
                localRepublishJob?.cancel()
                logCallStep("publish_local_stream_success", "status=$status")
            }
            "FAILED" -> {
                localPublishStarted = false
                hasPublishedLocalStream = false
                logCallStep(
                    "publish_local_stream_status_failed",
                    "status=$status info=$info attempts=$localRepublishAttempts"
                )
                // Бэкенд терминировал нашу публикацию (реджойн из терминального
                // статуса 2). Долбить republish бесполезно — сервер снова
                // терминирует. Идём по докам §4: шлём 2002 и ждём разрешения из
                // комнаты ожидания (2004/2005 → joinFromLobby опубликует).
                if (info?.contains("terminate", true) == true && !awaitingJoinApproval) {
                    awaitingJoinApproval = true
                    localRepublishJob?.cancel()
                    val dialogId = _uiState.value.dialogId
                    if (dialogId != null) requestJoinCall(dialogId)
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "Ожидаем разрешения на вход в звонок",
                    )
                    return
                }
                scheduleLocalRepublish(info)
            }
            "UNPUBLISHED", "STOPPED" -> {
                localPublishStarted = false
                hasPublishedLocalStream = false
                logCallStep("publish_local_stream_status_unpublished", "status=$status")
            }
        }
    }

    /**
     * Локальная публикация упала. Частая причина после реконнекта —
     * STREAM_NAME_ALREADY_IN_USE: на сервере ещё жива старая («призрачная»)
     * сессия с тем же именем потока (RoomApi публикует под фиксированным
     * roomId-login, уникальным именем не обойти). Сервер снимет её по keep-alive,
     * поэтому переиздаём поток с нарастающим backoff несколько раз — так «меня
     * снова видят» без тоста-провала. Тост показываем только когда попытки
     * исчерпаны. Не роняем звонок и не крутим бесконечно.
     */
    private fun scheduleLocalRepublish(failInfo: String? = null) {
        // Ждём разрешения из лобби (2002 отправлен) — republish бесполезен.
        if (awaitingJoinApproval) return
        val status = _uiState.value.status
        if (status != CallStatus.CONNECTING && status != CallStatus.IN_CALL) return
        if (localRepublishJob?.isActive == true) return
        if (restartStreamJob?.isActive == true) return
        // Причина FAILED определяет стратегию повтора:
        // - имя/сессия ещё заняты старой сессией (STREAM_NAME_ALREADY_IN_USE и т.п.)
        //   — сервер освободит по мере реапа старой сессии, поэтому имеет смысл
        //   ПОДОЖДАТЬ и повторить несколько раз с backoff (реджойн «меня не видят»).
        // - ICE/прочее — повтор не поможет, лишний churn роняет HAL эмулятора,
        //   поэтому одна попытка.
        val collision = failInfo?.let {
            it.contains("ALREADY_IN_USE", true) ||
                it.contains("SESSION_NOT_READY", true) ||
                it.contains("STREAM_NOT_FOUND", true)
        } ?: false
        val maxAttempts = if (collision) LOCAL_REPUBLISH_MAX_ATTEMPTS_COLLISION else LOCAL_REPUBLISH_MAX_ATTEMPTS
        if (localRepublishAttempts >= maxAttempts) {
            logCallStep("local_republish_giveup", "attempts=$localRepublishAttempts collision=$collision")
            _uiState.value = _uiState.value.copy(toastMessage = "Не удалось обновить медиа-поток")
            return
        }
        val attempt = localRepublishAttempts + 1
        localRepublishAttempts = attempt
        // Republish ВСЕГДА аудио-only: раз первичная видео-публикация упала, повтор
        // с видео — это только лишний churn (unpublish→publish), который роняет
        // сессию комнаты и весь звонок. Поднимаем стабильное аудио, звонок жив,
        // «меня слышат», видео пользователь включит вручную позже.
        val withVideo = false
        val delayMs = LOCAL_REPUBLISH_BASE_DELAY_MS * attempt
        localRepublishJob = viewModelScope.launch {
            delay(delayMs)
            val st = _uiState.value.status
            if (st != CallStatus.CONNECTING && st != CallStatus.IN_CALL) return@launch
            if (!flashphonerSessionManager.isRoomJoined()) {
                logCallStep("local_republish_skipped_no_room", "attempt=$attempt")
                return@launch
            }
            if (hasPublishedLocalStream) return@launch
            val session = _uiState.value.sessionState
            logCallStep("local_republish", "attempt=$attempt withVideo=$withVideo delayMs=$delayMs")
            // Если ушли в аудио-only — отражаем «камера выключена» в UI, чтобы
            // кнопка и локальная плитка соответствовали реальности.
            if (!withVideo && session.camOn) {
                _uiState.value = _uiState.value.copy(
                    sessionState = session.copy(camOn = false),
                )
            }
            restartLocalStream(
                audioEnabled = session.micOn,
                videoEnabled = withVideo,
            )
        }
    }

    private fun maybeSanitizeRemoteSdp(stream: Stream, label: String) {
        val config = currentConfig ?: return
        val streamObject = runCatching {
            val field = Stream::class.java.getDeclaredField("streamObject").apply { isAccessible = true }
            field.get(stream)
        }.getOrNull() ?: return
        val sdpField = runCatching {
            streamObject::class.java.getDeclaredField("sdp").apply { isAccessible = true }
        }.getOrNull() ?: return
        val sdp = sdpField.get(streamObject) as? String ?: return
        val hasRtcpMux = sdp.contains("a=rtcp-mux")
        val candidateLines = sdp.lineSequence().filter { it.startsWith("a=candidate") }.toList()
        val component2Candidates = candidateLines.filter { line ->
            val tokens = line.removePrefix("a=candidate:").trim().split("\\s+".toRegex())
            tokens.getOrNull(1) == "2"
        }
        logCallStep(
            "remote_sdp_summary",
            "label=$label sdpBytes=${sdp.length} candidates=${candidateLines.size} component2=${component2Candidates.size} rtcpMux=$hasRtcpMux"
        )
        if (!config.enableRtcpComponent2Filter || !hasRtcpMux || component2Candidates.isEmpty()) return

        val filteredSdp = sdp.lineSequence()
            .filterNot { line ->
                line.startsWith("a=candidate") && line.removePrefix("a=candidate:")
                    .trim()
                    .split("\\s+".toRegex())
                    .getOrNull(1) == "2"
            }
            .joinToString(separator = "\r\n")
        if (filteredSdp != sdp) {
            runCatching {
                sdpField.set(streamObject, filteredSdp)
                logCallStep("remote_sdp_filtered", "label=$label removed=${component2Candidates.size}")
            }.onFailure {
                logCallStep("remote_sdp_filter_failed", "label=$label error=${it.message}")
            }
        }
    }

    private fun logDiagnostics(trigger: String) {
        val config = currentConfig
        val status = _uiState.value.status
        val localStatus = localStream?.status
        val remoteStatus = remoteStream?.status
        Log.w(
            LOG_TAG,
            "Diagnostics trigger=$trigger opId=${callOperationId ?: "n/a"} " +
                    "room=${currentRoomName ?: "n/a"} stream=${currentStreamName ?: "n/a"} " +
                    "serverUrl=${config?.serverUrl ?: "n/a"} participants=$lastParticipantsCount " +
                    "status=$status localStreamStatus=$localStatus remoteStreamStatus=$remoteStatus"
        )
    }

    private fun logCallStep(step: String, details: String) {
        val opId = callOperationId ?: "n/a"
        Log.d(
            LOG_TAG,
            "opId=$opId step=$step room=${currentRoomName ?: "n/a"} " +
                    "sessionId=${currentStreamName ?: "n/a"} participants=$lastParticipantsCount details=$details"
        )
    }
}

data class CallUiState(
    val dialogId: Long? = null,
    val callTitle: String? = null,
    val participants: List<CallParticipant> = emptyList(),
    val status: CallStatus = CallStatus.DIALING,
    val callDurationSeconds: Int = 0,
    val sessionState: CallSessionState = CallSessionState(),
    val isRecording: Boolean = false,
    val isVideoCall: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String? = null,
    val isGroupCall: Boolean = false,
    val isCurrentUserAdmin: Boolean = false,
    val isCurrentUserOrganizer: Boolean = false,
    // Полный ростер для экрана «Участники звонка»: активные участники (status 5),
    // включая себя, и отдельно ожидающие в комнате ожидания (status 6). Плитки
    // видео используют [participants] (только удалённые), а этот экран — ростер.
    val rosterParticipants: List<CallParticipant> = emptyList(),
    val lobbyParticipants: List<CallParticipant> = emptyList(),
    val canRecordCall: Boolean = true,
    val toastMessage: String? = null,
    val audioRoutes: List<AudioRoute> = emptyList(),
    val currentAudioRouteId: String? = null,
    val currentAudioRouteName: String? = null,
    val currentCameraName: String? = null,
    val availableCameras: List<CameraDevice> = emptyList(),
    val currentCameraId: String? = null,
    val currentUserAvatarUrl: String? = null,
    // VU-метр: говорю ли сейчас я и кто из участников говорит (для пульсации).
    val isSelfSpeaking: Boolean = false,
    val speakingParticipantIds: Set<String> = emptySet(),
)

@Parcelize
data class CallParticipant(
    val id: String,
    val name: String?,
    val avatarUrl: String?,
    val isMuted: Boolean = false,
    // Включена ли камера участника (cType 2006 / participants-API). Когда false —
    // в плитке видеозвонка показываем аватар с затемнением, а не видео/loading.
    val camOn: Boolean = true,
    // Поднята ли рука участника (handUp из cType 2006) — показываем wave-бейдж.
    val handUp: Boolean = false,
    val roles: List<String> = emptyList(),
    val permits: List<String> = emptyList(),
    // Статус участника в звонке (docs/calls.md #190): 5 — участвует, 6 — в
    // комнате ожидания. Используется экраном «Участники звонка».
    val status: Int = 5,
) : Parcelable

enum class CallStatus {
    INCOMING,
    DIALING,
    CONNECTING,
    IN_CALL,
    FINISHED,
}

/** Камера в листе выбора: id = имя устройства из WebRTC-энумератора. */
data class CameraDevice(
    val id: String,
    val name: String,
    val isFront: Boolean,
)
