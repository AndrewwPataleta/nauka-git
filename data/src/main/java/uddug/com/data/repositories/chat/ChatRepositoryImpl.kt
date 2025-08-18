package uddug.com.data.repositories.chat

import uddug.com.data.mapper.mapChatDtoToDomain
import uddug.com.data.services.chat.ChatApiService
import uddug.com.data.services.models.request.chat.DeleteMessagesRequestDto
import uddug.com.data.services.models.request.chat.ReadMessagesRequestDto
import uddug.com.data.services.models.request.chat.UpdateMessageRequestDto
import uddug.com.data.services.models.response.chat.mapDialogInfoDtoToDomain
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
) : ChatRepository {

    override suspend fun getChats(): List<Chat> {
        return try {
            val chatDto = apiService.getDialogs()
            mapChatDtoToDomain(chatDto)
        } catch (e: Exception) {
            println("mapping error ${e.message}")
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
            apiService.getDialogMedia(dialogId, category = 1)
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

    override suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int) {
        val request = ReadMessagesRequestDto(dialogId, messages, status)
        apiService.markMessagesRead(request)
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
}
