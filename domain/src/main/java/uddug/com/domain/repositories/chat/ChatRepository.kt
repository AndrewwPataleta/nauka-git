package uddug.com.domain.repositories.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat

interface ChatRepository {
    suspend fun getChats(): List<Chat>
    suspend fun getMessages(currentUserId: String, dialogId: Long, limit: Int = 50): List<MessageChat>
    suspend fun getDialogInfo(dialogId: Long): DialogInfo
    suspend fun getMessagesWithOwnerInfo(currentUserId: String, dialogId: Long, limit: Int = 50): List<MessageChat>
    suspend fun getDialogMedia(dialogId: Long): List<MediaMessage>
    suspend fun createDialog(userId: Long): Long
}
