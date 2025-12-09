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
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
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
            sessionState = CallSessionState(micOn = true, camOn = isVideoCall),
            isRecording = false,
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
            sessionState = CallSessionState(micOn = true, camOn = isVideoCall),
            isRecording = false,
        )

        viewModelScope.launch {
            runCatching {
                val config = flashphonerConfigProvider.defaultConfig
                val username = resolveUsername()
                val streamName = buildStreamName(config, dialogId, username)
                mediaSessionId = streamName

                flashphonerSessionManager.prepareRoomManager(
                    serverUrl = config.serverUrl,
                    username = username,
                )

                flashphonerSessionManager.connectRoomManager(
                    createRoomManagerEvent(dialogId, streamName, isVideoCall)
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
            }.onFailure {
                handleCallFailure()
            }
        }
    }

    fun endCall() {
        lastCallParams = null
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
        )
        stopCallTimer()
        isCallStarted = false
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
                Log.d("CallVM", "RoomManager.onConnected status=${connection.status}")
                flashphonerSessionManager.joinRoom(
                    roomName = dialogId.toString(),
                    roomEvent = { room -> room.on(createRoomEvent(streamName)) },
                    onRoomReady = { publishLocalStream(streamName, isVideoCall) },
                )
            }

            override fun onDisconnection(connection: Connection) {
                Log.d("CallVM", "RoomManager.onDisconnection status=${connection.status}")
                attemptReconnectOrFail()
            }
        }
    }

    private fun createRoomEvent(streamName: String): RoomEvent {
        return object : RoomEvent {
            override fun onState(room: Room) {
                Log.d("CallVM", "RoomEvent.onState participants=${room.participants.size}")
                subscribeToParticipants(room.participants)
            }

            override fun onJoined(participant: Participant) {
                subscribeToParticipants(listOf(participant))
            }

            override fun onLeft(participant: Participant) {
                participant.stop()
            }

            override fun onPublished(participant: Participant) {
                subscribeToParticipants(listOf(participant))
            }

            override fun onFailed(room: Room, error: String) {
                Log.e("CallVM", "RoomEvent.onFailed error=$error")
                handleCallFailure(error)
            }

            override fun onMessage(message: Message) {
            }
        }
    }

    private fun publishLocalStream(streamName: String, isVideoCall: Boolean) {
        runCatching {
            flashphonerSessionManager.publishToCurrentRoom(streamName) {
                constraints = Constraints(true, isVideoCall)
            }
        }.onSuccess {
            Log.d("CallVM", "publishLocalStream success")
            _uiState.value = _uiState.value.copy(status = CallStatus.IN_CALL)
            startCallTimer()
        }.onFailure {
            Log.e("CallVM", "publishLocalStream failed", it)
            handleCallFailure()
        }
    }

    private fun restartLocalStream(videoEnabled: Boolean) {
        val streamName = mediaSessionId ?: return

        flashphonerSessionManager.unpublishCurrentStream()

        runCatching {
            flashphonerSessionManager.publishToCurrentRoom(streamName) {
                constraints = Constraints(true, videoEnabled)
            }
        }.onFailure {
            Log.e("CallVM", "restartLocalStream failed", it)
            handleCallFailure()
        }
    }


    private fun attemptReconnectOrFail() {
        val params = lastCallParams
        if (params == null || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            handleCallFailure()
            return
        }

        reconnectAttempts++
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
            runCatching { participant.play(null) }
        }
    }

    private fun handleCallFailure(@Suppress("UNUSED_PARAMETER") message: String? = null) {
        lastCallParams = null
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(
            status = CallStatus.FINISHED,
            isRecording = false,
        )
        stopCallTimer()
        isCallStarted = false
    }

    private fun resolveUsername(): String {
        return listOfNotNull(
            userUUIDCache.entity?.takeIf { it.isNotBlank() },
            userIdCache.entity?.takeIf { it.isNotBlank() },
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
        flashphonerSessionManager.reset()
        super.onCleared()
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
