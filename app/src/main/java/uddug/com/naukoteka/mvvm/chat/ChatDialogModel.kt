package uddug.com.naukoteka.mvvm.chat

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import uddug.com.data.repositories.chat.ChatRepository
import uddug.com.data.repositories.user_profile.UserProfileRepositoryImpl
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatDialogViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatRepository: ChatRepository,
    private val socketService: SocketService,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDialogUiState>(ChatDialogUiState.Loading)
    val uiState: StateFlow<ChatDialogUiState> = _uiState

    private val _events = MutableSharedFlow<ChatDialogEvents>()
    val events: SharedFlow<ChatDialogEvents> = _events.asSharedFlow()

    private var currentDialogID: Long? = null

    private var currentDialogInfo: DialogInfo? = null

    private var attachedFiles: MutableList<FileDescriptor> = mutableListOf()

    private var currentUser: UserProfileFullInfo? = null

    init {
        socketService.connect()
        socketService.setOnEvent("message") { message ->
            handleIncomingMessage(message)
        }
    }


    @SuppressLint("CheckResult")
    fun loadMessages(dialogId: Long) {
        _uiState.value = ChatDialogUiState.Loading
        viewModelScope.launch {
            try {
                userRepository.getProfileInfo().subscribeOn(Schedulers.io())
                    .subscribe({
                        currentUser = it
                        viewModelScope.launch {
                            val info = chatRepository.getDialogInfo(dialogId)
                            currentDialogInfo = info
                            val chats = it.id?.let { it1 ->
                                chatRepository.getMessagesWithOwnerInfo(
                                    currentUserId = it1,
                                    dialogId,
                                    50
                                )
                            }?.let {
                                currentDialogID = dialogId
                                val name: String = when {
                                    info.interlocutor != null -> info.interlocutor?.fullName.orEmpty()
                                    else -> info.name.orEmpty()
                                }
                                val image: String = when {
                                    info.interlocutor != null -> info.interlocutor?.image.orEmpty()
                                    else -> info.name.orEmpty()
                                }
                                _uiState.value = ChatDialogUiState.Success(
                                    chats = it,
                                    chatName = name,
                                    chatImage = image,
                                    isGroup = (info.users?.size ?: 0) > 2
                                )
                            }
                        }
                    }, {
                        it.printStackTrace()
                        _uiState.value = ChatDialogUiState.Error("Unknown error")
                    })

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = ChatDialogUiState.Error(e.message ?: "Unknown error")
            }

        }
    }

    fun updateCurrentMessage(newMessage: String) {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(currentMessage = newMessage)
        }
    }

    fun onChatDetailClick() {
        println("try emit detail ${currentDialogID}")
        currentDialogInfo?.let { info ->
            viewModelScope.launch {
                _events.emit(
                    ChatDialogEvents.OpenChatProfileDetail(
                        dialogId = 0,
                        dialogInfo = info
                    )
                )
            }
        }

    }

    fun attachFile(file: FileDescriptor) {
        attachedFiles.add(file)
    }

    fun clearAttachedFiles() {
        attachedFiles.clear()
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            if (text.isBlank() && attachedFiles.isEmpty()) {
                Log.d("ChatViewModel", "Message is blank and no files attached — skipping")
                return@launch
            }

            val message = ChatSocketMessage(
                dialog = dialog.id,
                text = text,
                owner = currentUser?.id.orEmpty(),
                // files = attachedFiles.toList()
            )
            val currentState = _uiState.value
            if (currentState is ChatDialogUiState.Success) {
                _uiState.value = currentState.copy(currentMessage = "")
            }
            Log.d("ChatViewModel", "Sending socket message: $message")
            socketService.sendMessage("message", message)
            clearAttachedFiles()
        }
    }

    private fun handleIncomingMessage(message: Any) {
        viewModelScope.launch {
            try {
                // Преобразуем сообщение из JSON в ChatSocketMessage
                val jsonString = when (message) {
                    is String -> message
                    is JSONObject -> message.toString()
                    else -> return@launch
                }

                val gson = Gson()
                val socketMessage = gson.fromJson(jsonString, ChatSocketMessage::class.java)


                val newMessage = MessageChat(
                    id = 0,
                    text = socketMessage.text,
                    type = MessageType.TEXT,
                    files = emptyList(),
                    ownerId = socketMessage.owner,
                    createdAt = Instant.now(),
                    readCount = 0,
                    ownerName = currentDialogInfo?.interlocutor?.fullName,
                    ownerAvatarUrl = currentDialogInfo?.interlocutor?.image,
                    ownerIsAdmin = false,
                    isMine = socketMessage.owner == currentUser?.id,
                    replyTo = null
                )

                val currentState = _uiState.value
                if (currentState is ChatDialogUiState.Success) {
                    val updatedChats = currentState.chats.toMutableList().apply {
                        add(0,newMessage)
                    }
                    _uiState.value = currentState.copy(chats = updatedChats)
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error processing incoming message", e)
            }
        }
    }

}

sealed class ChatDialogEvents {
    data class OpenChatProfileDetail(val dialogId: Long, val dialogInfo: DialogInfo) :
        ChatDialogEvents()
}

sealed class ChatDialogUiState {
    object Loading : ChatDialogUiState()
    data class Success(
        val chats: List<MessageChat>,
        val chatName: String,
        val chatImage: String,
        val isGroup: Boolean,
        val currentMessage: String = "",
    ) : ChatDialogUiState()

    data class Error(val message: String) : ChatDialogUiState()
}