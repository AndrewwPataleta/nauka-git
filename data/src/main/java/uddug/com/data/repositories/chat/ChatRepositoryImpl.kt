package uddug.com.data.repositories.chat

import uddug.com.data.mapper.mapChatDtoToDomain
import uddug.com.data.mapper.mapFolderDtoToDomain
import uddug.com.data.services.chat.ChatApiService
import uddug.com.data.services.models.request.chat.CreateDialogRequestDto
import uddug.com.data.services.models.request.chat.DeleteMessagesRequestDto
import uddug.com.data.services.models.request.chat.DialogImageRequestDto
import uddug.com.data.services.models.request.chat.PinMessageRequestDto
import uddug.com.data.services.models.request.chat.ReadMessagesRequestDto
import uddug.com.data.services.models.request.chat.UpdateMessageRequestDto
import uddug.com.data.services.models.request.chat.UsersStatusRequestDto
import uddug.com.data.services.models.response.chat.FileDto
import uddug.com.data.services.models.response.chat.UserStatusDto
import uddug.com.data.services.models.response.chat.mapDialogInfoDtoToDomain
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.data.utils.toDomain
import uddug.com.domain.entities.chat.File as ChatFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File as JavaFile
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

    override suspend fun createDialog(userId: Long): Long {
        return try {
            val dialog = apiService.createDialog(userId)
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

    override suspend fun updateMessage(messageId: Long, text: String): MessageChat {
        val dto = apiService.updateMessage(UpdateMessageRequestDto(messageId, text))
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
            apiService.searchUsers(searchField, limit, page).map { it.toDomain() }
        } catch (e: Exception) {
            println("search users error ${e.message}")
            emptyList()
        }
    }

    override suspend fun uploadFiles(files: List<JavaFile>, raw: Boolean): List<ChatFile> {
        val parts = files.map { file ->
            MultipartBody.Part.createFormData(
                name = "files",
                filename = file.name,
                body = file.asRequestBody("image/*".toMediaTypeOrNull())
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

private fun UserStatusDto.toDomain(): UserStatus =
    UserStatus(userId = userId, isOnline = status.isOnline, lastSeen = status.lastSeen)
