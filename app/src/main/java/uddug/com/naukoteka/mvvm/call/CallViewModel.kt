package uddug.com.naukoteka.mvvm.call

import android.app.Activity
import android.os.Parcelable
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
import uddug.com.naukoteka.flashphoner.FlashphonerConfig
import uddug.com.naukoteka.flashphoner.FlashphonerConfigProvider
import uddug.com.naukoteka.flashphoner.FlashphonerSessionManager

@HiltViewModel
class CallViewModel @Inject constructor(
    private val flashphonerConfigProvider: FlashphonerConfigProvider,
    private val flashphonerSessionManager: FlashphonerSessionManager,
    private val userIdCache: UserIdCache,
    private val userUUIDCache: UserUUIDCache,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private var isCallStarted = false
    private var callDurationJob: Job? = null

    fun startCall(
        activity: Activity,
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        participants: List<CallParticipant>? = null,
        callTitle: String? = null,
    ) {
        if (isCallStarted) return

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

        _uiState.value = CallUiState(
            dialogId = dialogId,
            callTitle = callTitle ?: contactName,
            participants = resolvedParticipants,
            status = CallStatus.DIALING,
        )

        viewModelScope.launch {
            runCatching {
                val config = flashphonerConfigProvider.defaultConfig
                val username = resolveUsername()
                val streamName = buildStreamName(config, dialogId, username)

                val roomManager = flashphonerSessionManager.prepareRoomManager(
                    activity = activity,
                    serverUrl = config.serverUrl,
                    username = username,
                )

                roomManager.on(createRoomManagerEvent(dialogId, streamName))
            }.onSuccess {
                _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
            }.onFailure {
                handleCallFailure()
            }
        }
    }

    fun endCall() {
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
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
    ): RoomManagerEvent {
        return object : RoomManagerEvent {
            override fun onConnected(connection: Connection) {
                flashphonerSessionManager.joinRoom(
                    roomName = dialogId.toString(),
                    roomEvent = { room -> room.on(createRoomEvent(streamName)) },
                    onRoomReady = { publishLocalStream(streamName) },
                )
            }

            override fun onDisconnection(connection: Connection) {
                handleCallFailure()
            }
        }
    }

    private fun createRoomEvent(streamName: String): RoomEvent {
        return object : RoomEvent {
            override fun onState(room: Room) {
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
                handleCallFailure(error)
            }

            override fun onMessage(message: Message) {
                // No-op. Signalling is handled by the backend, clients only need to join the room.
            }
        }
    }

    private fun publishLocalStream(streamName: String) {
        runCatching {
            flashphonerSessionManager.publishToCurrentRoom(streamName) {
                constraints = Constraints(true,  false)
            }
        }.onSuccess {
            _uiState.value = _uiState.value.copy(status = CallStatus.IN_CALL)
            startCallTimer()
        }.onFailure {
            handleCallFailure()
        }
    }

    private fun subscribeToParticipants(participants: Collection<Participant>) {
        participants.forEach { participant ->
            runCatching { participant.play(null) }
        }
    }

    private fun handleCallFailure(@Suppress("UNUSED_PARAMETER") message: String? = null) {
        flashphonerSessionManager.disconnectRoom()
        _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
        stopCallTimer()
        isCallStarted = false
    }

    private fun resolveUsername(): String {
        return listOfNotNull(
            userUUIDCache.entity?.takeIf { it.isNotBlank() },
            userIdCache.entity?.takeIf { it.isNotBlank() },
        ).firstOrNull() ?: "anonymous"
    }

    private fun buildStreamName(
        config: FlashphonerConfig,
        dialogId: Long,
        username: String,
    ): String {
        return listOf(config.streamName, dialogId, username)
            .joinToString(separator = "-")
    }
}

data class CallUiState(
    val dialogId: Long? = null,
    val callTitle: String? = null,
    val participants: List<CallParticipant> = emptyList(),
    val status: CallStatus = CallStatus.DIALING,
    val callDurationSeconds: Int = 0,
)

@Parcelize
data class CallParticipant(
    val id: String,
    val name: String?,
    val avatarUrl: String?,
    val isMuted: Boolean = false,
) : Parcelable

enum class CallStatus {
    DIALING,
    CONNECTING,
    IN_CALL,
    FINISHED,
}
