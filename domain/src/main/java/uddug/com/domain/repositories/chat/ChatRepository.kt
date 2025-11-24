package uddug.com.domain.repositories.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.domain.entities.chat.ChatFolderDetails
import uddug.com.domain.entities.chat.ChatFolderDialogsPage
import uddug.com.domain.entities.chat.ChatMediaCategory
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.entities.chat.PollOptionInput
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.entities.chat.File as ChatFile
import java.io.File as JavaFile

interface ChatRepository {
    suspend fun getChats(folderId: Long? = null): List<Chat>

    suspend fun getFolders(): List<ChatFolder>

    suspend fun reorderFolders(folderIds: List<Long>): List<ChatFolder>

    suspend fun createFolder(
        name: String,
        dialogIds: List<Long> = emptyList(),
        ord: Int? = null,
    ): ChatFolder

    suspend fun updateFolder(
        folderId: Long,
        name: String? = null,
        dialogIds: List<Long>? = null,
        ord: Int? = null,
    ): ChatFolder

    suspend fun deleteFolder(folderId: Long)

    suspend fun getFolder(folderId: Long): ChatFolderDetails

    suspend fun getFolderDialogs(
        folderId: Long,
        limit: Int,
        page: Int,
    ): ChatFolderDialogsPage

    suspend fun markFolderAsRead(folderId: Long)

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

    suspend fun getDialogInfoByPeer(interlocutorId: String): DialogInfo

    suspend fun getDialogMedia(
        dialogId: Long,
        category: ChatMediaCategory,
        limit: Int,
        page: Int,
        query: String?,
        sd: String?,
        ed: String?,
    ): List<MediaMessage>

    suspend fun searchMessages(
        currentUserId: String,
        dialogId: Long,
        searchField: String,
        limit: Int = 50,
        lastMessageId: Long? = null,
        sd: String? = null,
        ed: String? = null,
    ): List<MessageChat>

    suspend fun createDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String? = null,
    ): Long

    suspend fun createGroupDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String? = null,
    ): Long

    suspend fun updateDialogInfo(
        dialogId: Long,
        dialogName: String? = null,
        imageId: String? = null,
        removeImage: Boolean = false,
    ): DialogInfo

    suspend fun updateGroupDialog(
        dialogId: Long,
        dialogName: String? = null,
        imageId: String? = null,
        removeImage: Boolean = false,
        users: List<String>? = null,
    ): DialogInfo

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

    suspend fun deleteGroupDialog(dialogId: Long)

    suspend fun leaveGroupDialog(dialogId: Long)

    suspend fun makeDialogAdmin(dialogId: Long, userId: String)

    suspend fun removeDialogAdmin(dialogId: Long, userId: String)

    suspend fun pinMessage(dialogId: Long, messageId: Long)

    suspend fun unpinMessage(dialogId: Long, messageId: Long)

    suspend fun updateMessage(
        dialogId: Long,
        messageId: Long,
        text: String,
        files: List<FileDescriptor> = emptyList(),
    ): MessageChat

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false)

    suspend fun uploadFiles(files: List<JavaFile>, raw: Boolean = false): List<ChatFile>

    suspend fun getUsersStatus(userIds: List<String>): List<UserStatus>

    suspend fun searchDialogs(query: String): List<SearchDialog>

    suspend fun searchMessages(
        query: String,
        lastMessageId: Long? = null,
    ): List<SearchMessage>

    suspend fun createPoll(
        subject: String,
        isAnonymous: Boolean,
        multipleAnswers: Boolean,
        isQuiz: Boolean,
        options: List<PollOptionInput>,
    ): Poll

    suspend fun stopPoll(pollId: String): Boolean

    suspend fun deletePoll(pollId: String)

    suspend fun getPoll(pollId: String): Poll

    suspend fun answerPoll(pollId: String, optionIds: List<String>): Poll

    suspend fun getPollAnswerUsers(
        pollId: String,
        optionId: String,
        limit: Int = 10,
        page: Int = 1,
    ): List<UserProfileFullInfo>
}
