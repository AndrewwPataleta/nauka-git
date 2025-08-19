package uddug.com.domain.repositories.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.entities.chat.File as ChatFile
import java.io.File as JavaFile

interface ChatRepository {
    suspend fun getChats(folderId: Long? = null): List<Chat>

    suspend fun getFolders(): List<ChatFolder>

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

    suspend fun createDialog(userId: String): Long

    suspend fun createGroupDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String? = null,
    ): Long

    suspend fun searchUsers(searchField: String, limit: Int = 10, page: Int = 1): List<UserProfileFullInfo>

    suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int)

    suspend fun pinDialog(dialogId: Long)

    suspend fun unpinDialog(dialogId: Long)

    suspend fun setDialogUnread(dialogId: Long)

    suspend fun unsetDialogUnread(dialogId: Long)

    suspend fun disableNotifications(dialogId: Long)

    suspend fun enableNotifications(dialogId: Long)

    suspend fun blockDialog(dialogId: Long)

    suspend fun unblockDialog(dialogId: Long)

    suspend fun clearDialog(dialogId: Long)

    suspend fun deleteDialog(dialogId: Long)

    suspend fun pinMessage(dialogId: Long, messageId: Long)

    suspend fun unpinMessage(dialogId: Long, messageId: Long)

    suspend fun updateMessage(messageId: Long, text: String): MessageChat

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false)

    suspend fun uploadFiles(files: List<JavaFile>, raw: Boolean = false): List<ChatFile>

    suspend fun getUsersStatus(userIds: List<String>): List<UserStatus>
}
