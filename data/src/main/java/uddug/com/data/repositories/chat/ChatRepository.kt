package uddug.com.data.repositories.chat

import io.reactivex.Single
import uddug.com.data.mapper.mapChatDtoToDomain

import uddug.com.data.services.chat.ChatApiService
import uddug.com.data.services.models.response.chat.DialogInfoDto
import uddug.com.data.services.models.response.chat.mapDialogInfoDtoToDomain
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.updateOwnerInfoFromDialog
import uddug.com.domain.entities.feed.PostComment
import javax.inject.Inject

interface ChatRepository {
    suspend fun getChats(): List<Chat>
    suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
    ): List<MessageChat>

    suspend fun getDialogInfo(dialogId: Long): DialogInfo
    suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
    ): List<MessageChat>

    suspend fun getDialogMedia(
        dialogId: Long,
    ): List<MediaMessage>

    suspend fun createDialog(userId: Long): Long
}


class ChatRepositoryImpl @Inject constructor(
    private val apiService: ChatApiService,
) : ChatRepository {

    override suspend fun getChats(): List<Chat> {
        try {
            val chatDto = apiService.getDialogs()
            return mapChatDtoToDomain(chatDto)
        } catch (e: Exception) {
            println("mapping error ${e.message}")
            return emptyList()
        }

    }

    override suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int,
    ): List<MessageChat> {
        try {
            val messages = apiService.getMessages(dialogId, limit)
            return messages.map {
                it.toDomain(currentUserId)
            }
        } catch (e: Exception) {
            println("mapping error ${e.message}")
            return emptyList()
        }
    }

    override suspend fun getDialogInfo(dialogId: Long): DialogInfo {
        return try {
            val dialogInfoDto = apiService.getDialogInfo(dialogId)
            mapDialogInfoDtoToDomain(dialogInfoDto)
        } catch (e: Exception) {
            println("Error getting dialog info: ${e.message}")
            throw e // or return default DialogInfo
        }
    }

    override suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int,
    ): List<MessageChat> {
        val messages = getMessages(currentUserId, dialogId, limit)
        val dialogInfo = getDialogInfo(dialogId)

        return messages.map { message ->
            message.updateOwnerInfoFromDialog(dialogInfo)
        }
    }

    override suspend fun getDialogMedia(dialogId: Long): List<MediaMessage> {
        return try {
            val dialogInfoDto = apiService.getDialogMedia(dialogId, category = 1)
            return dialogInfoDto
        } catch (e: Exception) {
            println("Error getting dialog info: ${e.message}")
            throw e // or return default DialogInfo
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


}
