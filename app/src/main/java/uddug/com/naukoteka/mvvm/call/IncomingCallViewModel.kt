package uddug.com.naukoteka.mvvm.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val socketService: SocketService,
    private val chatInteractor: ChatInteractor,
    private val userRepository: UserProfileRepository,
    private val incomingCallStore: IncomingCallStore,
) : ViewModel() {

    private val gson = Gson()
    private val _events = MutableSharedFlow<IncomingCallEvent>(replay = 1)
    val events: SharedFlow<IncomingCallEvent> = _events.asSharedFlow()
    private val _callEndedEvents = MutableSharedFlow<CallEndedEvent>()
    val callEndedEvents: SharedFlow<CallEndedEvent> = _callEndedEvents.asSharedFlow()

    private var currentUserId: String? = null
    private val disposables = CompositeDisposable()

    init {
        disposables.add(
            userRepository.getProfileInfo()
                .subscribeOn(Schedulers.io())
                .subscribe({ user ->
                    currentUserId = user.id
                }, { error ->
                    Log.e("IncomingCallVM", "Failed to load user profile", error)
                })
        )

        socketService.connect()
        socketService.setOnEvent("message", LISTENER_TAG) { message ->
            handleIncomingMessage(message)
        }
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
                if (jsonObject.has("action")) return@launch

                val socketMessage = gson.fromJson(jsonString, ChatSocketMessage::class.java)
                val dialogId = socketMessage.dialog ?: return@launch

                if (socketMessage.cType == 6) {
                    _callEndedEvents.emit(CallEndedEvent(dialogId = dialogId))
                    return@launch
                }

                val isCallMessage = socketMessage.cType in listOf(2, 3) &&
                    socketMessage.files.isNullOrEmpty() &&
                    (socketMessage.text?.contains("звонок", ignoreCase = true) == true ||
                        socketMessage.text.equals(CALL_STARTED_TEXT, ignoreCase = true))

                if (!isCallMessage) return@launch
                if (socketMessage.owner == currentUserId) return@launch

                val dialogInfo = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
                val contactName = dialogInfo?.interlocutor?.fullName ?: dialogInfo?.name
                val avatarUrl = dialogInfo?.dialogImage?.path ?: dialogInfo?.interlocutor?.image
                val callTitle = dialogInfo?.name ?: contactName
                // DialogInfo.type == 1 is a 1-to-1 dialog; anything else is a
                // group chat, so the call must open in the group tile layout.
                val isGroupCall = (dialogInfo?.type ?: 1) != 1

                val event = IncomingCallEvent(
                    dialogId = dialogId,
                    contactName = contactName,
                    avatarUrl = avatarUrl,
                    callTitle = callTitle,
                    isGroupCall = isGroupCall,
                    // cType 3 — видеозвонок, 2 — аудио. Без этого флага
                    // принимающая сторона считала бы любой звонок видео и
                    // включала камеру.
                    isVideoCall = socketMessage.cType == 3,
                )
                incomingCallStore.save(event)
                _events.emit(event)
            } catch (e: Exception) {
                Log.e("IncomingCallVM", "Error processing incoming call", e)
            }
        }
    }

    fun emitPendingIncomingCallIfAny() {
        viewModelScope.launch {
            incomingCallStore.get()?.let { pending ->
                _events.emit(pending)
                incomingCallStore.clear()
            }
        }
    }

    fun clearPendingIncomingCall() {
        incomingCallStore.clear()
        // Drop the replayed event as well: ContainerActivity re-collects
        // `events` inside repeatOnLifecycle(STARTED), so a retained replay value
        // re-opens the call screen on every resume/unlock for a call that was
        // already accepted or declined.
        _events.resetReplayCache()
    }

    override fun onCleared() {
        disposables.clear()
        socketService.removeEvent("message", LISTENER_TAG)
        super.onCleared()
    }

    companion object {
        private const val CALL_STARTED_TEXT = "Звонок начался"
        private const val LISTENER_TAG = "IncomingCallViewModel"
    }
}

data class IncomingCallEvent(
    val dialogId: Long,
    val contactName: String?,
    val avatarUrl: String?,
    val callTitle: String?,
    val isGroupCall: Boolean = false,
    val isVideoCall: Boolean = false,
)

data class CallEndedEvent(
    val dialogId: Long,
)
