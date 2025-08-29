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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import java.time.Instant
import java.time.Duration
import java.io.File
import uddug.com.naukoteka.mvvm.chat.ContactInfo
import javax.inject.Inject

@HiltViewModel
class ChatDialogViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService,
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

    private var currentUser: UserProfileFullInfo? = null

    init {
        socketService.connect()
        socketService.setOnEvent("message") { message ->
            handleIncomingMessage(message)
        }
    }


    @SuppressLint("CheckResult")
    fun loadMessages(dialogId: Long) {
        _uiState.value = ChatDialogUiState.Loading()
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                userRepository.getProfileInfo().subscribeOn(Schedulers.io())
                    .subscribe({
                        currentUser = it
                        viewModelScope.launch {
                            val info = chatInteractor.getDialogInfo(dialogId)
                            currentDialogInfo = info
                            currentDialogID = dialogId

                            val name: String = when {
                                info.interlocutor != null -> info.interlocutor?.fullName.orEmpty()
                                else -> info.name.orEmpty()
                            }
                            val image: String = when {
                                info.interlocutor != null -> info.interlocutor?.image.orEmpty()
                                else -> info.name.orEmpty()
                            }
                            val firstParticipantName = info.users?.firstOrNull()?.fullName.orEmpty()
                            var status: String? = null
                            val isGroup = (info.users?.size ?: 0) > 2
                            if (!isGroup) {
                                val userId = info.interlocutor?.userId
                                if (userId != null) {
                                    try {
                                        val userStatus = chatInteractor.getUsersStatus(listOf(userId)).firstOrNull()
                                        status = if (userStatus?.isOnline == true) {
                                            "Онлайн"
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

                            it.id?.let { currentUserId ->
                                val messages = chatInteractor.getMessagesWithOwnerInfo(
                                    currentUserId = currentUserId,
                                    dialogId = dialogId,
                                    limit = 50,
                                    lastMessageId = null,
                                ).sortedBy { it.createdAt }
                                val elapsed = System.currentTimeMillis() - startTime
                                if (elapsed < 500L) delay(500L - elapsed)
                                _uiState.value = ChatDialogUiState.Success(
                                    chats = messages,
                                    chatName = name,
                                    chatImage = image,
                                    isGroup = isGroup,
                                    firstParticipantName = firstParticipantName,
                                    status = status
                                )
                                markMessagesRead(dialogId, messages)
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

    @SuppressLint("CheckResult")
    fun loadMessagesByPeer(interlocutorId: String) {
        _uiState.value = ChatDialogUiState.Loading()
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            try {
                userRepository.getProfileInfo().subscribeOn(Schedulers.io())
                    .subscribe({ user ->
                        currentUser = user
                        viewModelScope.launch {
                            val info = chatInteractor.getDialogInfoByPeer(interlocutorId)
                            currentDialogInfo = info
                            val dialogId = info.id

                            val name = info.interlocutor?.fullName.orEmpty()
                            val image = info.interlocutor?.image.orEmpty()
                            val firstParticipantName = info.users?.firstOrNull()?.fullName.orEmpty()
                            var status: String? = null
                            val isGroup = (info.users?.size ?: 0) > 2
                            if (!isGroup) {
                                val userId = info.interlocutor?.userId
                                if (userId != null) {
                                    try {
                                        val userStatus = chatInteractor.getUsersStatus(listOf(userId)).firstOrNull()
                                        status = if (userStatus?.isOnline == true) {
                                            "Онлайн"
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
                                user.id?.let { currentUserId ->
                                    val messages = chatInteractor.getMessagesWithOwnerInfo(
                                        currentUserId = currentUserId,
                                        dialogId = dialogId,
                                        limit = 50,
                                        lastMessageId = null,
                                    ).sortedBy { it.createdAt }
                                    val elapsed = System.currentTimeMillis() - startTime
                                    if (elapsed < 500L) delay(500L - elapsed)
                                    _uiState.value = ChatDialogUiState.Success(
                                        chats = messages,
                                        chatName = name,
                                        chatImage = image,
                                        isGroup = isGroup,
                                        firstParticipantName = firstParticipantName,
                                        status = status
                                    )
                                    markMessagesRead(dialogId, messages)
                                }
                            } else {
                                val elapsed = System.currentTimeMillis() - startTime
                                if (elapsed < 500L) delay(500L - elapsed)
                                _uiState.value = ChatDialogUiState.Success(
                                    chats = emptyList(),
                                    chatName = name,
                                    chatImage = image,
                                    isGroup = false,
                                    firstParticipantName = firstParticipantName,
                                    status = status
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
            _uiState.value = currentState.copy(replyMessage = message)
        }
    }

    fun clearReplyMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(replyMessage = null)
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
                // Ignored: network or API error
            } finally {
                clearSelection()
            }
        }
    }

    private fun determineFileType(file: File): Int {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif" -> 1
            "mp4", "mkv", "mov", "avi", "wmv", "flv", "webm" -> 30
            "m4a", "aac", "amr", "3gp" -> 21
            "mp3", "wav", "flac", "ogg", "oga", "opus" -> 20
            else -> 1
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val currentState = _uiState.value
            val replyId = (currentState as? ChatDialogUiState.Success)?.replyMessage?.id

            if (text.isBlank() && attachedFiles.isEmpty()) {
                Log.d("ChatViewModel", "Message is blank and no files attached — skipping")
                return@launch
            }
            val uploaded = if (attachedFiles.isNotEmpty()) {
                try {
                    chatInteractor.uploadFiles(attachedFiles)
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else emptyList()

            val fileDescriptors = uploaded.mapIndexed { index, uploadedFile ->
                val type = attachedFiles.getOrNull(index)?.let { determineFileType(it) } ?: 100
                FileDescriptor(
                    id = uploadedFile.id,
                    fileType = type
                )
            }

            val cType = if (fileDescriptors.isEmpty()) 1 else 3

            val message = if (dialog.id != 0L) {
                ChatSocketMessage(
                    dialog = dialog.id,
                    cType = cType,
                    text = text,
                    owner = currentUser?.id.orEmpty(),
                    files = fileDescriptors.ifEmpty { null },
                    answered = replyId
                )
            } else {
                val peer = dialog.interlocutor?.userId ?: return@launch
                ChatSocketMessage(
                    interlocutor = peer,
                    cType = cType,
                    text = text,
                    owner = currentUser?.id.orEmpty(),
                    files = fileDescriptors.ifEmpty { null },
                    answered = replyId
                )
            }
            if (currentState is ChatDialogUiState.Success) {
                _uiState.value = currentState.copy(currentMessage = "")
            }
            Log.d("ChatViewModel", "Sending socket message: $message")
            socketService.sendMessage("message", message)
            clearAttachedFiles()
            clearAttachedContact()
            clearReplyMessage()
        }
    }

    fun sendVoiceMessage(file: File) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val uploaded = try {
                chatInteractor.uploadFiles(listOf(file))
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
            val descriptor = uploaded.firstOrNull()?.let { uploadedFile ->
                FileDescriptor(
                    id = uploadedFile.id,
                    fileType = determineFileType(file)
                )
            }
            val message = if (dialog.id != 0L) {
                ChatSocketMessage(
                    dialog = dialog.id,
                    cType = 4,
                    text = "",
                    owner = currentUser?.id.orEmpty(),
                    files = descriptor?.let { listOf(it) }
                )
            } else {
                val peer = dialog.interlocutor?.userId ?: return@launch
                ChatSocketMessage(
                    interlocutor = peer,
                    cType = 4,
                    text = "",
                    owner = currentUser?.id.orEmpty(),
                    files = descriptor?.let { listOf(it) }
                )
            }
            socketService.sendMessage("message", message)
        }
    }

    private fun markMessagesRead(dialogId: Long, messages: List<MessageChat>) {
        val messageIds = messages.filter { !it.isMine && (it.readCount ?: 0) == 0 }.map { it.id }
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
        return try {
            val instant = Instant.parse(lastSeen)
            val duration = Duration.between(instant, Instant.now())
            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()
            val weeks = days / 7
            when {
                minutes < 60 -> "был ${minutes} мин. назад"
                hours < 24 -> "был ${hours} ч. назад"
                days < 7 -> "был ${days} д. назад"
                else -> "был ${weeks} нед. назад"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun handleIncomingMessage(message: Any) {
        viewModelScope.launch {
            try {
                // Преобразуем сообщение из JSON
                val jsonString = when (message) {
                    is String -> message
                    is JSONObject -> message.toString()
                    else -> return@launch
                }
                val jsonObject = JSONObject(jsonString)

                // Обрабатываем действие удаления сообщений
                if (jsonObject.has("action")) {
                    val action = jsonObject.getJSONObject("action")
                    if (action.optString("type") == "delete") {
                        val ids = mutableListOf<Long>()
                        val array = action.optJSONArray("messages")
                        if (array != null) {
                            for (i in 0 until array.length()) {
                                ids.add(array.getLong(i))
                            }
                        } else {
                            action.optLong("messageId").takeIf { it != 0L }?.let { ids.add(it) }
                        }
                        val currentState = _uiState.value
                        if (currentState is ChatDialogUiState.Success && ids.isNotEmpty()) {
                            val updatedChats = currentState.chats.filterNot { ids.contains(it.id) }
                            _uiState.value = currentState.copy(chats = updatedChats)
                        }
                    }
                    return@launch
                }

                val gson = Gson()
                val socketMessage = gson.fromJson(jsonString, ChatSocketMessage::class.java)

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
                    replyTo = replyPreview
                )

                val currentState = _uiState.value
                if (currentState is ChatDialogUiState.Success) {
                    val updatedChats = currentState.chats.toMutableList().apply {
                        add(newMessage)
                    }
                    _uiState.value = currentState.copy(chats = updatedChats)
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error processing incoming message", e)
            }
        }
    }

}

private const val READ_STATUS = 2

sealed class ChatDialogEvents {
    data class OpenChatProfileDetail(val dialogId: Long, val dialogInfo: DialogInfo) :
        ChatDialogEvents()
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
    ) : ChatDialogUiState()

    data class Error(val message: String) : ChatDialogUiState()
}