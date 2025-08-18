package uddug.com.domain.repositories.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat

interface ChatRepository {
    suspend fun getChats(): List<Chat>

    suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat>

    suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat>

    suspend fun getDialogInfo(dialogId: Long): DialogInfo

    suspend fun getDialogMedia(dialogId: Long): List<MediaMessage>

    suspend fun createDialog(userId: Long): Long

    suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int)

    suspend fun updateMessage(messageId: Long, text: String): MessageChat

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false)
}
