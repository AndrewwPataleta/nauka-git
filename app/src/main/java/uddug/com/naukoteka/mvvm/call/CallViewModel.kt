package uddug.com.naukoteka.mvvm.call

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.flashphoner.fpwcsapi.bean.Connection
import com.flashphoner.fpwcsapi.constraints.Constraints
import com.flashphoner.fpwcsapi.room.Message
import com.flashphoner.fpwcsapi.room.Participant
import com.flashphoner.fpwcsapi.room.Room
import com.flashphoner.fpwcsapi.room.RoomEvent
import com.flashphoner.fpwcsapi.room.RoomManagerEvent
import com.flashphoner.fpwcsapi.session.Stream
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
import org.webrtc.SurfaceViewRenderer
import java.util.UUID
import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.cache.user_uuid.UserUUIDCache
import uddug.com.domain.entities.call.CallSessionState
import uddug.com.domain.repositories.call.CallRepository
import uddug.com.naukoteka.flashphoner.FlashphonerConfig
import uddug.com.naukoteka.flashphoner.FlashphonerConfigProvider
import uddug.com.naukoteka.flashphoner.FlashphonerSessionManager


@HiltViewModel
class CallViewModel @Inject constructor(
    private val flashphonerConfigProvider: FlashphonerConfigProvider,
    private val flashphonerSessionManager: FlashphonerSessionManager,
    private val userIdCache: UserIdCache,
    private val userUUIDCache: UserUUIDCache,
    private val callRepository: CallRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private var isCallStarted = false
    private var callDurationJob: Job? = null
    private var mediaSessionId: String? = null
    private var lastCallParams: CallParams? = null
    private var reconnectAttempts = 0
    private var remoteRenderer: SurfaceViewRenderer? = null
    private val participantStreams = mutableMapOf<String, Stream>()
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

    fun showIncomingCall(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        participants: List<CallParticipant>? = null,
        callTitle: String? = null,
        isVideoCall: Boolean = false,
    ) {
        if (isCallStarted || _uiState.value.status == CallStatus.IN_CALL) return

        if (dialogId <= 0) {
            _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
            return
        }

        val resolvedParticipants = participants?.takeIf { it.isNotEmpty() }
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

        lastCallParams = CallParams(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
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
    ) {
        if (isCallStarted) return

        if (resetReconnectAttempts) {
            reconnectAttempts = 0
        }
        lastCallParams = CallParams(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            participants = participants,
            callTitle = callTitle,
            isVideoCall = isVideoCall,
        )

        if (dialogId <= 0) {
            _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
            return
        }
        isCallStarted = true
        lastParticipantsCount = 0

        val resolvedParticipants = participants?.takeIf { it.isNotEmpty() }
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
        )

        viewModelScope.launch {
            runCatching {
                val config = flashphonerConfigProvider.defaultConfig
                val username = resolveUsername()
                val streamName = buildStreamName(config, dialogId, username)
                val operationId = UUID.randomUUID().toString()
                callOperationId = operationId
                currentConfig = config
                currentRoomName = dialogId.toString()
                currentStreamName = streamName
                mediaSessionId = streamName

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
                    "serverUrl=${config.serverUrl} username=$username streamName=$streamName"
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
            }.onFailure {
                handleCallFailure("Failed to connect to call service.")
            }
        }
    }

    fun endCall() {
        lastCallParams = null
        clearVideoStreams()
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
            errorMessage = null,
        )
        stopCallTimer()
        isCallStarted = false
        stopParticipantWatchdog()
        callOperationId = null
        currentConfig = null
        currentRoomName = null
        currentStreamName = null
        clearVideoState()
    }

    private fun startCallTimer() {
        callDurationJob?.cancel()
        callDurationJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                _uiState.value = _uiState.value.copy(callDurationSeconds = seconds)
                delay(1_000)
                seconds++
            }
        }
    }

    private fun stopCallTimer() {
        callDurationJob?.cancel()
        callDurationJob = null
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
                flashphonerSessionManager.joinRoom(
                    roomName = dialogId.toString(),
                    roomEvent = { room -> room.on(createRoomEvent(streamName)) },
                    onRoomReady = { publishLocalStream(streamName, isVideoCall) },
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

    private fun createRoomEvent(streamName: String): RoomEvent {
        return object : RoomEvent {
            override fun onState(room: Room) {
                val remoteParticipants = room.participants.filterNot(::isSelfParticipant)
                lastParticipantsCount = remoteParticipants.size
                logCallStep(
                    "room_state",
                    "participants=${room.participants.size} remoteParticipants=${remoteParticipants.size} room=${room.name}"
                )
                subscribeToParticipants(remoteParticipants)
                checkParticipantWatchdog()
            }

            override fun onJoined(participant: Participant) {
                logCallStep("participant_joined", "participant=${participant.name}")
                if (!isSelfParticipant(participant)) {
                    lastParticipantsCount = maxOf(lastParticipantsCount, 1)
                    subscribeToParticipants(listOf(participant))
                    checkParticipantWatchdog()
                }
            }

            override fun onLeft(participant: Participant) {
                logCallStep("participant_left", "participant=${participant.name}")
                removeParticipantStream(participant)
                participant.stop()
                if (participant == pendingRemoteParticipant || participant.stream == remoteStream) {
                    pendingRemoteParticipant = null
                    remoteStream = null
                }
            }

            override fun onPublished(participant: Participant) {
                logCallStep("participant_published", "participant=${participant.name}")
                if (!isSelfParticipant(participant)) {
                    lastParticipantsCount = maxOf(lastParticipantsCount, 1)
                    subscribeToParticipants(listOf(participant))
                    checkParticipantWatchdog()
                }
            }

            override fun onFailed(room: Room, error: String) {
                Log.e("CallVM", "RoomEvent.onFailed error=$error")
                handleCallFailure("Room join failed: $error")
            }

            override fun onMessage(message: Message) {
            }
        }
    }

    private fun publishLocalStream(streamName: String, isVideoCall: Boolean) {
        runCatching {
            localStream = flashphonerSessionManager.publishToCurrentRoom(streamName, localRenderer) {
                constraints = Constraints(true, isVideoCall)
            }
        }.onSuccess {
            logCallStep("publish_local_stream_success", "streamName=$streamName")
            attachStreamDiagnostics(localStream, "local_publish")
            _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
        }.onFailure {
            Log.e("CallVM", "publishLocalStream failed", it)
            handleCallFailure("Failed to publish local media.")
        }
    }

    private fun restartLocalStream(videoEnabled: Boolean) {
        val streamName = mediaSessionId ?: return

        flashphonerSessionManager.unpublishCurrentStream()

        runCatching {
            localStream = flashphonerSessionManager.publishToCurrentRoom(streamName, localRenderer) {
                constraints = Constraints(true, videoEnabled)
            }
        }.onFailure {
            Log.e("CallVM", "restartLocalStream failed", it)
            handleCallFailure("Failed to restart local media.")
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
        updateCallState(updatedState)
    }

    fun toggleCamera() {
        val currentState = _uiState.value.sessionState
        val updatedState = currentState.copy(camOn = !currentState.camOn)
        updateCallState(updatedState)

        if (_uiState.value.status == CallStatus.IN_CALL) {
            restartLocalStream(updatedState.camOn)
        }
    }

    fun toggleRecording() {
        val dialogId = _uiState.value.dialogId ?: return
        if (_uiState.value.status != CallStatus.IN_CALL) return

        viewModelScope.launch {
            if (_uiState.value.isRecording) {
                runCatching { callRepository.stopRecording(dialogId) }
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(isRecording = false)
                    }
            } else {
                runCatching { callRepository.startRecording(dialogId) }
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(isRecording = true)
                    }
            }
        }
    }

    private fun updateCallState(newState: CallSessionState) {
        val dialogId = _uiState.value.dialogId ?: return
        val sessionId = mediaSessionId ?: return
        val userId = userIdCache.entity ?: resolveUsername()

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
            }
        }
    }

    private fun subscribeToParticipants(participants: Collection<Participant>) {
        participants.forEach { participant ->
            if (isSelfParticipant(participant)) {
                logCallStep("participant_self_ignored", "participant=${participant.name}")
                return@forEach
            }
            val key = participant.streamName ?: participant.name ?: return@forEach
            if (participantStreams.containsKey(key)) return@forEach

            val renderer = if (primaryStreamKey == null) remoteRenderer else null
            val stream = runCatching { participant.play(renderer) }.getOrNull() ?: return@forEach
            participantStreams[key] = stream
            if (primaryStreamKey == null) {
                primaryStreamKey = key
                remoteStream = stream
            }
            attachStreamDiagnostics(stream, "remote_play:$key")
            markRemoteParticipantConnected()
        }
    }


    private fun removeParticipantStream(participant: Participant) {
        val key = participant.streamName ?: participant.name ?: return
        val removed = participantStreams.remove(key) ?: return
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
        participantStreams.values.forEach { it.stop() }
        participantStreams.clear()
        primaryStreamKey = null
        remoteRenderer = null
    }

    private fun handleCallFailure(@Suppress("UNUSED_PARAMETER") message: String? = null) {
        logCallStep("call_failed", message ?: "unknown")
        lastCallParams = null
        clearVideoStreams()
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
            errorMessage = message,
        )
        stopCallTimer()
        isCallStarted = false
        stopParticipantWatchdog()
        callOperationId = null
        currentConfig = null
        currentRoomName = null
        currentStreamName = null
        clearVideoState()
    }

    private fun resolveUsername(): String {
        return listOfNotNull(
            userIdCache.entity?.takeIf { it.isNotBlank() },
            userUUIDCache.entity?.takeIf { it.isNotBlank() },
        ).firstOrNull() ?: "anonymous"
    }

    private data class CallParams(
        val dialogId: Long,
        val contactName: String?,
        val avatarUrl: String?,
        val participants: List<CallParticipant>?,
        val callTitle: String?,
        val isVideoCall: Boolean,
    )

    private companion object {
        const val MAX_RECONNECT_ATTEMPTS = 1
        const val PARTICIPANT_WAIT_TIMEOUT_MS = 10_000L
        const val LOG_TAG = "CallFlow"
    }

    private fun buildStreamName(
        config: FlashphonerConfig,
        dialogId: Long,
        username: String,
    ): String {
        return listOf(config.streamName, dialogId, username)
            .joinToString(separator = "-")
    }

    override fun onCleared() {
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

    private fun clearVideoState() {
        localStream = null
        remoteStream = null
        pendingRemoteParticipant = null
        clearRenderers()
    }

    private fun isSelfParticipant(participant: Participant): Boolean {
        val username = resolveUsername()
        return participant.name == username || participant.streamName?.contains(username) == true
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
        }
    }

    private fun attachStreamDiagnostics(stream: Stream?, label: String) {
        if (stream == null) return
        stream.on { _, status ->
            logCallStep("stream_status", "label=$label status=$status")
            maybeSanitizeRemoteSdp(stream, label)
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
)

@Parcelize
data class CallParticipant(
    val id: String,
    val name: String?,
    val avatarUrl: String?,
    val isMuted: Boolean = false,
) : Parcelable

enum class CallStatus {
    INCOMING,
    DIALING,
    CONNECTING,
    IN_CALL,
    FINISHED,
}
