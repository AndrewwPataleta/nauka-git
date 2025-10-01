package uddug.com.data.repositories.chat

import uddug.com.data.mapper.mapChatDtoToDomain
import uddug.com.data.mapper.mapFolderDtoToDomain
import uddug.com.data.services.chat.ChatApiService
import uddug.com.data.services.models.request.chat.CreateDialogRequestDto
import uddug.com.data.services.models.request.chat.DeleteMessagesRequestDto
import uddug.com.data.services.models.request.chat.DialogImageRequestDto
import uddug.com.data.services.models.request.chat.PinMessageRequestDto
import uddug.com.data.services.models.request.chat.ReadMessagesRequestDto
import uddug.com.data.services.models.request.chat.UpdateMessageFileDto
import uddug.com.data.services.models.request.chat.UpdateMessageRequestDto
import uddug.com.data.services.models.request.chat.UsersStatusRequestDto
import uddug.com.data.services.models.response.chat.FileDto
import uddug.com.data.services.models.response.chat.UserStatusDto
import uddug.com.data.services.models.response.chat.mapDialogInfoDtoToDomain
import uddug.com.data.services.models.response.chat.SearchDialogDto
import uddug.com.data.services.models.response.chat.SearchMessageDto
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.data.utils.toDomain
import uddug.com.domain.entities.chat.File as ChatFile
import java.time.Instant
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File as JavaFile
import java.net.URLConnection
import java.util.Locale
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
) : ChatRepository {

    override suspend fun getChats(folderId: Long?): List<Chat> {
        return try {
            val chatDto = apiService.getDialogs(folderId)
            mapChatDtoToDomain(chatDto)
        } catch (e: Exception) {
            println("mapping error ${e.message}")
            emptyList()
        }
    }

    override suspend fun getFolders(): List<ChatFolder> {
        return try {
            apiService.getFolders().folders.map { mapFolderDtoToDomain(it) }
        } catch (e: Exception) {
            println("get folders error ${e.message}")
            emptyList()
        }
    }

    override suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int,
        lastMessageId: Long?,
    ): List<MessageChat> {
        return try {
            val messages = apiService.getMessages(dialogId, limit, lastMessageId)
            messages.map { it.toDomain(currentUserId) }
        } catch (e: Exception) {
            println("mapping error ${e.message}")
            emptyList()
        }
    }

    override suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int,
        lastMessageId: Long?,
    ): List<MessageChat> {
        val messages = getMessages(currentUserId, dialogId, limit, lastMessageId)
        val dialogInfo = getDialogInfo(dialogId)
        return messages.map { message -> message.updateOwnerInfoFromDialog(dialogInfo) }
    }

    override suspend fun getDialogInfo(dialogId: Long): DialogInfo {
        return try {
            val dialogInfoDto = apiService.getDialogInfo(dialogId)
            mapDialogInfoDtoToDomain(dialogInfoDto)
        } catch (e: Exception) {
            println("Error getting dialog info: ${e.message}")
            throw e
        }
    }

    override suspend fun getDialogInfoByPeer(interlocutorId: String): DialogInfo {
        return try {
            val dialogInfoDto = apiService.getDialogInfoByPeer(interlocutorId)
            mapDialogInfoDtoToDomain(dialogInfoDto)
        } catch (e: Exception) {
            println("Error getting dialog info by peer: ${e.message}")
            throw e
        }
    }


    override suspend fun getDialogMedia(
        dialogId: Long,
        category: Int,
        limit: Int,
        page: Int,
        query: String?,
        sd: String?,
        ed: String?,
    ): List<MediaMessage> {
        return try {
            apiService.getDialogMedia(
                dialogId,
                category = category,
                limit = limit,
                page = page,
                query = query,
                sd = sd,
                ed = ed,
            )
        } catch (e: Exception) {
            println("Error getting dialog media: ${e.message}")
            throw e
        }
    }

    override suspend fun searchMessages(
        currentUserId: String,
        dialogId: Long,
        searchField: String,
        limit: Int,
        lastMessageId: Long?,
        sd: String?,
        ed: String?,
    ): List<MessageChat> {
        return try {
            val messages = apiService.searchMessages(
                dialogId,
                searchField = searchField,
                limit = limit,
                lastMessageId = lastMessageId,
                sd = sd,
                ed = ed,
            )
            messages.map { it.toDomain(currentUserId) }
        } catch (e: Exception) {
            println("Error searching messages: ${e.message}")
            emptyList()
        }
    }

    override suspend fun createDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String?,
    ): Long {
        return try {
            val request = CreateDialogRequestDto(
                dialogName = dialogName,
                dialogImage = imageId?.let { DialogImageRequestDto(it) },
                userRoles = userRoles,
            )
            val dialog = apiService.createDialog(request)
            dialog.id
        } catch (e: Exception) {
            println("Error creating dialog: ${e.message}")
            throw e
        }
    }

    override suspend fun createGroupDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String?,
    ): Long {
        return try {
            val request = CreateDialogRequestDto(
                dialogName = dialogName,
                dialogImage = imageId?.let { DialogImageRequestDto(it) },
                userRoles = userRoles,
            )
            val dialog = apiService.createGroupDialog(request)
            dialog.id
        } catch (e: Exception) {
            println("Error creating group dialog: ${e.message}")
            throw e
        }
    }

    override suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int) {
        val request = ReadMessagesRequestDto(dialogId, messages, status)
        apiService.markMessagesRead(request)
    }

    override suspend fun pinDialog(dialogId: Long) {
        apiService.pinDialog(dialogId)
    }

    override suspend fun unpinDialog(dialogId: Long) {
        apiService.unpinDialog(dialogId)
    }

    override suspend fun setDialogUnread(dialogId: Long) {
        apiService.setDialogUnread(dialogId)
    }

    override suspend fun unsetDialogUnread(dialogId: Long) {
        apiService.unsetDialogUnread(dialogId)
    }

    override suspend fun disableNotifications(dialogId: Long) {
        apiService.disableNotifications(dialogId)
    }

    override suspend fun enableNotifications(dialogId: Long) {
        apiService.enableNotifications(dialogId)
    }

    override suspend fun blockDialog(dialogId: Long) {
        apiService.blockDialog(dialogId)
    }

    override suspend fun unblockDialog(dialogId: Long) {
        apiService.unblockDialog(dialogId)
    }

    override suspend fun clearDialog(dialogId: Long) {
        apiService.clearDialog(dialogId)
    }

    override suspend fun deleteDialog(dialogId: Long) {
        apiService.deleteDialog(dialogId)
    }

    override suspend fun pinMessage(dialogId: Long, messageId: Long) {
        apiService.pinMessage(PinMessageRequestDto(dialogId, messageId))
    }

    override suspend fun unpinMessage(dialogId: Long, messageId: Long) {
        apiService.unpinMessage(PinMessageRequestDto(dialogId, messageId))
    }

    override suspend fun updateMessage(
        dialogId: Long,
        messageId: Long,
        text: String,
        files: List<FileDescriptor>,
    ): MessageChat {
        val dto = apiService.updateMessage(
            UpdateMessageRequestDto(
                dialogId = dialogId,
                messageId = messageId,
                updatedText = text,
                files = files.map { file ->
                    UpdateMessageFileDto(
                        id = file.id,
                        fileType = file.fileType,
                    )
                },
            )
        )
        return dto.toDomain(dto.ownerId)
    }

    override suspend fun deleteMessage(messageId: Long, forMe: Boolean) {
        apiService.deleteMessage(messageId, forMe)
    }

    override suspend fun deleteMessages(messages: List<Long>, forMe: Boolean) {
        apiService.deleteMessages(DeleteMessagesRequestDto(messages), forMe)
    }

    override suspend fun searchUsers(searchField: String, limit: Int, page: Int): List<UserProfileFullInfo> {
        return try {
            apiService.searchUsers(searchField, limit, page).users.map { it.toDomain() }
        } catch (e: Exception) {
            println("search users error ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchDialogs(query: String): List<SearchDialog> {
        return try {
            apiService.searchDialogs(query = query).map { it.toDomain() }
        } catch (e: Exception) {
            println("search dialogs error ${e.message}")
            emptyList()
        }
    }

    override suspend fun searchMessages(
        query: String,
        lastMessageId: Long?,
    ): List<SearchMessage> {
        return try {
            apiService.searchMessages(query = query, lastMessageId = lastMessageId)
                .map { it.toDomain() }
        } catch (e: Exception) {
            println("search messages error ${e.message}")
            emptyList()
        }
    }

    override suspend fun uploadFiles(files: List<JavaFile>, raw: Boolean): List<ChatFile> {
        val parts = files.map { file ->
            val mediaType = determineMimeType(file).toMediaType()
            MultipartBody.Part.createFormData(
                name = "files",
                filename = file.name,
                body = file.asRequestBody(mediaType)
            )
        }
        return try {
            apiService.uploadFiles(parts, raw).map { it.toDomain() }
        } catch (e: Exception) {
            println("upload files error ${e.message}")
            emptyList()
        }
    }

    override suspend fun getUsersStatus(userIds: List<String>): List<UserStatus> {
        return try {
            val request = UsersStatusRequestDto(userIds)
            apiService.getUsersStatus(request).map { it.toDomain() }
        } catch (e: Exception) {
            println("get users status error ${e.message}")
            emptyList()
        }
    }
}

private fun FileDto.toDomain(): ChatFile = ChatFile(
    id = id,
    path = path,
    fileName = fileName,
    contentType = contentType,
    fileSize = fileSize,
    fileType = fileType,
    fileKind = fileKind,
    duration = duration,
    viewCount = viewCount,
)

private fun determineMimeType(file: JavaFile): String {
    val fileName = file.name
    val extension = fileName.substringAfterLast('.', "").lowercase(Locale.getDefault())
    val mimeTypeFromExtension = if (extension.isNotEmpty()) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    } else {
        null
    }
    return mimeTypeFromExtension
        ?: URLConnection.guessContentTypeFromName(fileName)
        ?: "application/octet-stream"
}

private fun UserStatusDto.toDomain(): UserStatus =
    UserStatus(userId = userId, isOnline = status.isOnline, lastSeen = status.lastSeen)

private fun SearchDialogDto.toDomain(): SearchDialog =
    SearchDialog(
        dialogId = dialogId,
        dialogType = dialogType,
        messageId = messageId,
        fullName = fullName,
        image = image?.path,
        createdAt = Instant.parse(createdAt),
    )

private fun SearchMessageDto.toDomain(): SearchMessage =
    SearchMessage(
        dialogId = dialogId,
        messageId = messageId,
        fullName = fullName,
        image = image?.path,
        userId = userId,
        isOnline = status.isOnline,
        lastSeen = status.lastSeen,
        text = text,
        createdAt = Instant.parse(createdAt),
    )
