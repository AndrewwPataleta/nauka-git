package uddug.com.naukoteka.mvvm.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    private val gson = Gson()
    private val _events = MutableSharedFlow<IncomingCallEvent>()
    val events: SharedFlow<IncomingCallEvent> = _events.asSharedFlow()
    private val _callEndedEvents = MutableSharedFlow<CallEndedEvent>()
    val callEndedEvents: SharedFlow<CallEndedEvent> = _callEndedEvents.asSharedFlow()

    private var currentUserId: String? = null

    init {
        userRepository.getProfileInfo()
            .subscribeOn(Schedulers.io())
            .subscribe({ user ->
                currentUserId = user.id
            }, { error ->
                Log.e("IncomingCallVM", "Failed to load user profile", error)
            })

        socketService.connect()
        socketService.setOnEvent("message") { message ->
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
                    socketMessage.text?.contains("звонок", ignoreCase = true) == true

                if (!isCallMessage) return@launch
                if (socketMessage.owner == currentUserId) return@launch

                val dialogInfo = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
                val contactName = dialogInfo?.interlocutor?.fullName ?: dialogInfo?.name
                val avatarUrl = dialogInfo?.dialogImage?.path ?: dialogInfo?.interlocutor?.image
                val callTitle = dialogInfo?.name ?: contactName

                _events.emit(
                    IncomingCallEvent(
                        dialogId = dialogId,
                        contactName = contactName,
                        avatarUrl = avatarUrl,
                        callTitle = callTitle,
                    )
                )
            } catch (e: Exception) {
                Log.e("IncomingCallVM", "Error processing incoming call", e)
            }
        }
    }
}

data class IncomingCallEvent(
    val dialogId: Long,
    val contactName: String?,
    val avatarUrl: String?,
    val callTitle: String?,
)

data class CallEndedEvent(
    val dialogId: Long,
)
