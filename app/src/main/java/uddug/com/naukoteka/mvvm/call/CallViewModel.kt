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
    private var callStartedAtMs: Long? = null
    private var mediaSessionId: String? = null
    private var lastCallParams: CallParams? = null
    private var reconnectAttempts = 0
    private var remoteRenderer: SurfaceViewRenderer? = null
    private val participantStreams = mutableMapOf<String, Stream>()
    private val participantRenderers = mutableMapOf<String, SurfaceViewRenderer>()
    private val participantHandles = mutableMapOf<String, Participant>()
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
    private var hasPublishedLocalStream = false
    private var hasRetriedPublish = false
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
        lastParticipantsCount = 0
        localPublishStarted = false
        hasPublishedLocalStream = false
        hasRetriedPublish = false

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
        flashphonerSessionManager.disconnectRoom()
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
        callOperationId = null
        currentConfig = null
        currentRoomName = null
        currentStreamName = null
        currentLogin = null
        profileUserId = null
        hasPublishedLocalStream = false
        hasRetriedPublish = false
        clearVideoState()
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
    }

    private fun stopCallTimer() {
        callDurationJob?.cancel()
        callDurationJob = null
        callStartedAtMs = null
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
        logCallStep("publish_local_stream_start", "streamName=$streamName isVideoCall=$isVideoCall")

        Log.d("CallVM", "PUBLISHING STREAM: $streamName")
        logCallStep(
            "publish_local_stream_params",
            "roomName=${currentRoomName ?: "n/a"} mediaSessionId=${mediaSessionId ?: "n/a"} customParams=constraints(audio=$audioEnabled,video=$isVideoCall)"
        )
        runCatching {
            localStream = flashphonerSessionManager.publishToCurrentRoom(streamName, localRenderer) {
                constraints = Constraints(audioEnabled, isVideoCall)
            }
        }.onSuccess {
            val actualMediaSessionId = localStream?.id
            if (!actualMediaSessionId.isNullOrBlank()) {
                mediaSessionId = actualMediaSessionId
            }
            logCallStep(
                "publish_local_stream_identifiers",
                "requestedStreamName=$streamName actualStreamName=${localStream?.name ?: "n/a"} actualMediaSessionId=${localStream?.id ?: "n/a"}"
            )
            logCallStep("publish_local_stream_callback_started", "streamName=$streamName")
            attachStreamDiagnostics(localStream, "local_publish")
            _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
        }.onFailure {
            localPublishStarted = false
            Log.e("CallVM", "publishLocalStream failed", it)
            logCallStep("publish_local_stream_failure", "streamName=$streamName error=${it.message}")
            handleCallFailure("Failed to publish local media.")
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
                logCallStep(
                    "publish_local_stream_restart_identifiers",
                    "requestedStreamName=$streamName actualStreamName=${localStream?.name ?: "n/a"} actualMediaSessionId=${localStream?.id ?: "n/a"}"
                )
                attachStreamDiagnostics(localStream, "local_publish")
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
        logCallStep("reconnect_attempt", "attempt=$reconnectAttempts")
        isCallStarted = false
        flashphonerSessionManager.reset()
        startCall(
            dialogId = params.dialogId,
            contactName = params.contactName,
            avatarUrl = params.avatarUrl,
            participants = params.participants,
            callTitle = params.callTitle,
            isVideoCall = params.isVideoCall,
            resetReconnectAttempts = false,
            isGroupCall = params.isGroupCall,
        )
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

        // Upgrading a call that is not (yet) video to video: the published local
        // stream has no video track, so unmuteVideo() on it is a no-op and the
        // user would stay invisible. Republish with a video constraint and mark
        // the call as video so the UI switches to the video layout and — via the
        // persisted ActiveCallState — stays video across a later reconnect.
        // Only reachable in group calls (the camera button is hidden in 1-to-1
        // audio calls), so the "republish ends a 1-to-1 call" caveat on
        // restartLocalStream does not apply here.
        if (enabling && !_uiState.value.isVideoCall) {
            logCallStep("camera_upgrade_to_video", "republishing local stream with video")
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

    /** Переключает фронтальную/основную камеру у публикуемого локального потока. */
    fun switchCamera() {
        val stream = localStream ?: return
        runCatching {
            stream.switchCamera(object : CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    this@CallViewModel.isFrontCamera = isFrontCamera
                    _uiState.value = _uiState.value.copy(
                        currentCameraName = cameraLabel(isFrontCamera),
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
        removed.switchRenderer(null)
        if (remoteStream == removed) {
            remoteStream = null
        }
        if (primaryStreamKey == key) {
            primaryStreamKey = participantStreams.keys.firstOrNull()
            val nextStream = primaryStreamKey?.let { participantStreams[it] }
            if (remoteRenderer != null && nextStream != null) {
                nextStream.switchRenderer(remoteRenderer)
            }
        }
        removed.stop()
    }

    private fun clearVideoStreams() {
        val streamsToStop = LinkedHashSet<Stream>().apply {
            addAll(participantStreams.values)
            localStream?.let(::add)
            remoteStream?.let(::add)
        }

        streamsToStop.forEach {
            it.switchRenderer(null)
            it.stop()
        }

        participantStreams.clear()
        participantRenderers.clear()
        participantHandles.clear()
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
        val id = withContext(Dispatchers.IO) { userProfileRepository.getProfileInfo().await() }.id
        profileUserId = id
        _uiState.value = _uiState.value.copy(currentUserId = id)
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
        const val MAX_RECONNECT_ATTEMPTS = 1
        const val PARTICIPANT_WAIT_TIMEOUT_MS = 10_000L
        const val LOG_TAG = "CallFlow"
        const val ROLE_ORGANIZER = "37:301"
        const val ROLE_ADMIN = "37:302"
        const val PERMIT_MANAGE_PARTICIPANTS = "82:611"
        const val PERMIT_RECORD_CALL = "82:608"
        const val STATUS_PARTICIPATING = 5
        const val CTYPE_STATE_CHANGED = 2006
        const val CTYPE_PERMITS_CHANGED = 2007
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
        localStream?.switchRenderer(renderer)
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
        remoteStream?.switchRenderer(renderer)
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
        localStream?.switchRenderer(null)
        localRenderer = null
    }

    fun clearRemoteRenderer() {
        remoteStream?.switchRenderer(null)
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
            findStreamForParticipant(participantId)?.switchRenderer(renderer)
        }
    }

    fun releaseParticipantRenderer(participantId: String) {
        participantRenderers.remove(participantId)
        findStreamForParticipant(participantId)?.switchRenderer(null)
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
            CTYPE_PERMITS_CHANGED -> refreshParticipants()
            CTYPE_RECORD_STATUS -> handleRecordStatus(json)
        }
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
                if (!micOn && _uiState.value.sessionState.micOn) {
                    _uiState.value = _uiState.value.copy(
                        sessionState = _uiState.value.sessionState.copy(micOn = false),
                        toastMessage = "Администратор отключил ваш микрофон",
                    )
                }
            } else {
                val participant = _uiState.value.participants.find { it.id == userId }
                if (participant != null) {
                    val updatedParticipants = _uiState.value.participants.map {
                        if (it.id == userId) it.copy(isMuted = !micOn) else it
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

            _uiState.value = _uiState.value.copy(
                participants = enriched + missing,
                isCurrentUserAdmin = isAdmin,
                canRecordCall = canRecord,
            )
            logCallStep(
                "participants_refreshed",
                "dialogId=$dialogId participants=${enriched.size + missing.size} " +
                    "apiCount=${apiParticipants.size} rosterCount=${dialogUsers.size} isAdmin=$isAdmin"
            )
        }
    }

    private fun attachStreamDiagnostics(stream: Stream?, label: String) {
        if (stream == null) return
        stream.on { _, status ->
            logCallStep("stream_status", "label=$label status=$status")
            handleLocalPublishStatus(label, status.toString())
            maybeSanitizeRemoteSdp(stream, label)
        }
    }

    private fun handleLocalPublishStatus(label: String, status: String) {
        if (label != "local_publish") return
        when (status.uppercase()) {
            "PUBLISHING" -> logCallStep("publish_local_stream_status_publishing", "status=$status")
            "PUBLISHED" -> {
                hasPublishedLocalStream = true
                logCallStep("publish_local_stream_success", "status=$status")
            }
            "FAILED" -> {
                localPublishStarted = false
                hasPublishedLocalStream = false
                logCallStep(
                    "publish_local_stream_status_failed",
                    "status=$status hasRetriedPublish=$hasRetriedPublish"
                )
                // Do NOT end the call on stream FAILED. This status commonly
                // fires as a transient state while `restartLocalStream`
                // unpublishes → republishes on mic/camera toggle. Ending the
                // call here caused users to get kicked out on toggle.
                //
                // A publish also FAILs right after a reconnect while a stale
                // session with the same stream name ("$userId#$dialogId") is
                // still being torn down on the server — the classic "I rejoined
                // but nobody hears or sees me" case. Retry the publish once; by
                // the time it runs the ghost session has usually timed out.
                // Guarded by hasRetriedPublish so we never loop, and skipped
                // while a deliberate restart is already in flight.
                val canRetry = !hasRetriedPublish &&
                    restartStreamJob?.isActive != true &&
                    (_uiState.value.status == CallStatus.CONNECTING ||
                        _uiState.value.status == CallStatus.IN_CALL)
                if (canRetry) {
                    hasRetriedPublish = true
                    val session = _uiState.value.sessionState
                    logCallStep("publish_local_stream_retry", "retrying after FAILED")
                    restartLocalStream(
                        audioEnabled = session.micOn,
                        videoEnabled = _uiState.value.isVideoCall,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        toastMessage = "Не удалось обновить медиа-поток",
                    )
                }
            }
            "UNPUBLISHED", "STOPPED" -> {
                localPublishStarted = false
                hasPublishedLocalStream = false
                logCallStep("publish_local_stream_status_unpublished", "status=$status")
            }
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
    val canRecordCall: Boolean = true,
    val toastMessage: String? = null,
    val audioRoutes: List<AudioRoute> = emptyList(),
    val currentAudioRouteId: String? = null,
    val currentAudioRouteName: String? = null,
    val currentCameraName: String? = null,
)

@Parcelize
data class CallParticipant(
    val id: String,
    val name: String?,
    val avatarUrl: String?,
    val isMuted: Boolean = false,
    val roles: List<String> = emptyList(),
    val permits: List<String> = emptyList(),
) : Parcelable

enum class CallStatus {
    INCOMING,
    DIALING,
    CONNECTING,
    IN_CALL,
    FINISHED,
}
