package uddug.com.naukoteka.mvvm.chat

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.mvvm.chat.ContactInfo
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
import java.time.Instant
import java.io.File
import java.io.EOFException
import java.util.Locale
import uddug.com.domain.entities.chat.File as ChatFile
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import javax.inject.Inject

private const val IMAGE_FILE_TYPE = 1
private const val VIDEO_FILE_TYPE = 30
private const val VOICE_FILE_TYPE = 21
private const val AUDIO_FILE_TYPE = 20
private const val DOCUMENT_FILE_TYPE = 100

private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "mov", "avi", "wmv", "flv", "webm")
private val VOICE_EXTENSIONS = setOf("m4a", "aac", "amr", "3gp")
private val AUDIO_EXTENSIONS = setOf("mp3", "wav", "flac", "ogg", "oga", "opus")
private val DOCUMENT_EXTENSIONS = setOf(
    "pdf",
    "doc",
    "docx",
    "xls",
    "xlsx",
    "ppt",
    "pptx",
    "txt",
    "zip",
    "rar",
    "7z",
    "rtf",
    "csv"
)

@HiltViewModel
class ChatDialogViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService,
    private val chatStatusFormatter: ChatStatusFormatter,
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

    private var selectedUser: UserProfileFullInfo? = null

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
                                            chatStatusFormatter.online()
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
                                            chatStatusFormatter.online()
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

    fun attachUserContact(user: UserProfileFullInfo) {
        selectedUser = user
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(selectedContact = user)
        }
    }

    fun clearSelectedContact() {
        selectedUser = null
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(selectedContact = null)
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
            _uiState.value = currentState.copy(
                replyMessage = message,
                editingMessage = null,
                currentMessage = if (currentState.editingMessage != null) "" else currentState.currentMessage,
            )
        }
    }

    fun clearReplyMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(replyMessage = null)
        }
    }

    fun startEditingMessage(message: MessageChat) {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success && message.isMine) {
            attachedFiles.clear()
            attachedContact = null
            selectedUser = null
            _uiState.value = currentState.copy(
                editingMessage = message,
                currentMessage = message.text.orEmpty(),
                attachedFiles = emptyList(),
                replyMessage = null,
                attachedContact = null,
                selectedContact = null,
            )
        }
    }

    fun clearEditingMessage() {
        val currentState = _uiState.value
        if (currentState is ChatDialogUiState.Success) {
            _uiState.value = currentState.copy(
                editingMessage = null,
                currentMessage = ""
            )
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
                
            } finally {
                clearSelection()
            }
        }
    }

    private fun determineFileType(file: File): Int {
        val extension = file.extension.lowercase()
        return when {
            extension in IMAGE_EXTENSIONS -> IMAGE_FILE_TYPE
            extension in VIDEO_EXTENSIONS -> VIDEO_FILE_TYPE
            extension in VOICE_EXTENSIONS -> VOICE_FILE_TYPE
            extension in AUDIO_EXTENSIONS -> AUDIO_FILE_TYPE
            extension in DOCUMENT_EXTENSIONS -> DOCUMENT_FILE_TYPE
            extension.isBlank() -> IMAGE_FILE_TYPE
            else -> DOCUMENT_FILE_TYPE
        }
    }

    private fun ChatFile.toFileDescriptor(): FileDescriptor {
        return FileDescriptor(
            id = id,
            fileType = resolveExistingFileType(),
        )
    }

    private fun ChatFile.resolveExistingFileType(): Int {
        fileType?.let { return it }

        val contentType = contentType?.lowercase(Locale.ROOT)
        val extension = fileName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase(Locale.ROOT)

        return when {
            extension != null && extension in VOICE_EXTENSIONS -> VOICE_FILE_TYPE
            extension != null && extension in AUDIO_EXTENSIONS -> AUDIO_FILE_TYPE
            extension != null && extension in VIDEO_EXTENSIONS -> VIDEO_FILE_TYPE
            extension != null && extension in IMAGE_EXTENSIONS -> IMAGE_FILE_TYPE
            contentType?.startsWith("audio/") == true -> AUDIO_FILE_TYPE
            contentType?.startsWith("video/") == true -> VIDEO_FILE_TYPE
            contentType?.startsWith("image/") == true -> IMAGE_FILE_TYPE
            else -> DOCUMENT_FILE_TYPE
        }
    }

    private fun buildUserContactPayload(user: UserProfileFullInfo): String? {
        val json = JSONObject()
        user.id?.let { json.put("id", it) }
        user.fullName?.let { json.put("fullName", it) }
        user.nickname?.let { json.put("nickname", it) }
        user.phone?.let { json.put("phone", it) }
        user.image?.path?.let { json.put("image", it) }
        return if (json.length() == 0) null else json.toString()
    }

    private fun buildPhoneContactPayload(contact: ContactInfo): String {
        return JSONObject().apply {
            put("name", contact.name)
            put("phone", contact.phone)
        }.toString()
    }

    private fun createContactSocketMessage(
        dialog: DialogInfo,
        payload: String,
        replyId: Long?,
    ): ChatSocketMessage? {
        return if (dialog.id != 0L) {
            ChatSocketMessage(
                dialog = dialog.id,
                cType = 7,
                text = payload,
                owner = currentUser?.id.orEmpty(),
                answered = replyId
            )
        } else {
            val peer = dialog.interlocutor?.userId ?: return null
            ChatSocketMessage(
                interlocutor = peer,
                cType = 7,
                text = payload,
                owner = currentUser?.id.orEmpty(),
                answered = replyId
            )
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val currentState = _uiState.value
            val successState = currentState as? ChatDialogUiState.Success
            val replyId = successState?.replyMessage?.id

            selectedUser?.let { user ->
                val payload = buildUserContactPayload(user) ?: return@launch
                val message = createContactSocketMessage(dialog, payload, replyId) ?: return@launch
                socketService.sendMessage("message", message)
                selectedUser = null
                if (currentState is ChatDialogUiState.Success) {
                    _uiState.value = currentState.copy(
                        currentMessage = "",
                        selectedContact = null
                    )
                }
                clearReplyMessage()
                return@launch
            }

            attachedContact?.let { contact ->
                val payload = buildPhoneContactPayload(contact)
                val message = createContactSocketMessage(dialog, payload, replyId) ?: return@launch
                socketService.sendMessage("message", message)
                attachedContact = null
                if (currentState is ChatDialogUiState.Success) {
                    _uiState.value = currentState.copy(
                        currentMessage = "",
                        attachedContact = null
                    )
                }
                clearReplyMessage()
                return@launch
            }

            if (text.isBlank() && attachedFiles.isEmpty()) {
                Log.d("ChatViewModel", "Message is blank and no files attached — skipping")
                return@launch
            }
            val editingMessage = successState?.editingMessage

            if (editingMessage != null) {
                val updatedMessage = try {
                    chatInteractor.updateMessage(
                        dialogId = dialog.id,
                        messageId = editingMessage.id,
                        text = text,
                        files = editingMessage.files.mapNotNull { it.toFileDescriptor() },
                    )
                } catch (e: EOFException) {
                    Log.w(
                        "ChatViewModel",
                        "Empty response received when updating message, using local data",
                        e
                    )
                    editingMessage.copy(text = text)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Failed to update message", e)
                    return@launch
                }

                val updatedChats = successState.chats.map { existing ->
                    if (existing.id == updatedMessage.id) {
                        existing.copy(
                            text = updatedMessage.text,
                            type = updatedMessage.type,
                            files = updatedMessage.files,
                            readCount = updatedMessage.readCount,
                            createdAt = updatedMessage.createdAt,
                            ownerId = updatedMessage.ownerId,
                            isMine = updatedMessage.isMine
                        )
                    } else {
                        existing
                    }
                }
                _uiState.value = successState.copy(
                    chats = updatedChats,
                    currentMessage = "",
                    editingMessage = null
                )
                return@launch
            }

            val uploadRequiresRaw = attachedFiles.any { determineFileType(it) != IMAGE_FILE_TYPE }
            val uploaded = if (attachedFiles.isNotEmpty()) {
                try {
                    chatInteractor.uploadFiles(attachedFiles, uploadRequiresRaw)
                } catch (e: Exception) {
                    e.printStackTrace()
                    emptyList()
                }
            } else emptyList()

            val fileDescriptors = uploaded.mapIndexed { index, uploadedFile ->
                val type = attachedFiles.getOrNull(index)?.let { determineFileType(it) } ?: DOCUMENT_FILE_TYPE
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
            if (successState != null) {
                _uiState.value = successState.copy(currentMessage = "")
            }
            Log.d("ChatViewModel", "Sending socket message: $message")
            socketService.sendMessage("message", message)
            clearAttachedFiles()
            clearReplyMessage()
        }
    }

    fun sendVoiceMessage(file: File) {
        viewModelScope.launch {
            val dialog = currentDialogInfo ?: return@launch
            val uploaded = try {
                val requiresRawUpload = determineFileType(file) != IMAGE_FILE_TYPE
                chatInteractor.uploadFiles(listOf(file), requiresRawUpload)
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
        return runCatching { Instant.parse(lastSeen) }
            .map { chatStatusFormatter.formatLastSeen(it, GENERIC) }
            .getOrDefault("")
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

                val newMessage = socketMessage
                    .toMessageChat(replyPreview)
                    .let { message ->
                        currentDialogInfo?.let { info ->
                            message.updateOwnerInfoFromDialog(info)
                        } ?: message
                    }

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

    private fun ChatSocketMessage.toMessageChat(replyPreview: MessageChat?): MessageChat {
        val createdAtInstant = parseInstantOrNow(createdAt)
        val attachments = files?.mapNotNull { it.toChatFile() } ?: emptyList()
        val isMineMessage = owner == currentUser?.id
        val type = if (cType == 5) MessageType.SYSTEM else MessageType.TEXT

        return MessageChat(
            id = id ?: 0L,
            text = text,
            type = type,
            files = attachments,
            ownerId = owner,
            createdAt = createdAtInstant,
            readCount = read ?: if (isMineMessage) 1 else 0,
            ownerName = ownerName,
            ownerAvatarUrl = ownerAvatarUrl,
            ownerIsAdmin = false,
            isMine = isMineMessage,
            replyTo = replyPreview
        )
    }

    private fun FileDescriptor.toChatFile(): ChatFile? {
        val filePath = path ?: return null
        return ChatFile(
            id = id,
            path = filePath,
            fileName = fileName,
            contentType = contentType,
            fileSize = fileSize,
            fileType = fileType,
            fileKind = fileKind,
            duration = duration,
            viewCount = viewCount
        )
    }

    private fun parseInstantOrNow(value: String?): Instant {
        if (value.isNullOrBlank()) return Instant.now()
        return runCatching { Instant.parse(value) }.getOrElse { Instant.now() }
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
        val selectedContact: UserProfileFullInfo? = null,
        val editingMessage: MessageChat? = null,
    ) : ChatDialogUiState()

    data class Error(val message: String) : ChatDialogUiState()
}
