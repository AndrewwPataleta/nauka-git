package uddug.com.domain.interactors.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.repositories.chat.ChatRepository

class ChatInteractor(
    private val repository: ChatRepository
) {
    suspend fun getChats(): List<Chat> = repository.getChats()

    suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50
    ): List<MessageChat> = repository.getMessages(currentUserId, dialogId, limit)

    suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50
    ): List<MessageChat> = repository.getMessagesWithOwnerInfo(currentUserId, dialogId, limit)

    suspend fun getDialogInfo(dialogId: Long): DialogInfo = repository.getDialogInfo(dialogId)

    suspend fun getDialogMedia(dialogId: Long): List<MediaMessage> = repository.getDialogMedia(dialogId)

    suspend fun createDialog(userId: Long): Long = repository.createDialog(userId)
}
