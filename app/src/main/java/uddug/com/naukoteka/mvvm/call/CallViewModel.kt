package uddug.com.naukoteka.mvvm.call

import android.app.Activity
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.parcelize.Parcelize
import uddug.com.naukoteka.flashphoner.FlashphonerConfigProvider
import uddug.com.naukoteka.flashphoner.FlashphonerEnvironment
import uddug.com.naukoteka.flashphoner.FlashphonerSessionManager

@HiltViewModel
class CallViewModel @Inject constructor(
    private val flashphonerEnvironment: FlashphonerEnvironment,
    private val flashphonerConfigProvider: FlashphonerConfigProvider,
    private val flashphonerSessionManager: FlashphonerSessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState

    private var isCallStarted = false
    private var callDurationJob: Job? = null

    fun startCall(
        activity: Activity,
        contactName: String?,
        avatarUrl: String?,
        participants: List<CallParticipant>? = null,
        callTitle: String? = null,
    ) {
        if (isCallStarted) return
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
            callTitle = callTitle ?: contactName,
            participants = resolvedParticipants,
            status = CallStatus.DIALING,
        )

        viewModelScope.launch {
            runCatching {
                val config = flashphonerConfigProvider.defaultConfig
                flashphonerEnvironment.ensureInitialised(activity)
                flashphonerSessionManager.prepareSession(activity, config.serverUrl)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(status = CallStatus.CONNECTING)
                _uiState.value = _uiState.value.copy(status = CallStatus.IN_CALL)
                startCallTimer()
            }.onFailure {
                _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
                stopCallTimer()
            }
        }
    }

    fun endCall() {
        flashphonerSessionManager.disconnectSession()
        _uiState.value = _uiState.value.copy(status = CallStatus.FINISHED)
        stopCallTimer()
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
}

data class CallUiState(
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
