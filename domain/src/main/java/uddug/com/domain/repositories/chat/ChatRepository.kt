package uddug.com.domain.repositories.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.profile.UserProfileFullInfo

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

    suspend fun getDialogMedia(
        dialogId: Long,
        category: Int,
        limit: Int,
        page: Int,
        query: String?,
        sd: String?,
        ed: String?,
    ): List<MediaMessage>

    suspend fun createDialog(userId: Long): Long

    suspend fun createGroupDialog(dialogName: String?, userIds: List<Long>, imageId: String? = null): Long

    suspend fun searchUsers(searchField: String, limit: Int = 10, page: Int = 1): List<UserProfileFullInfo>

    suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int)

    suspend fun updateMessage(messageId: Long, text: String): MessageChat

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false)
}
