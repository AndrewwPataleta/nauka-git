package uddug.com.domain.interactors.chat

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
import uddug.com.domain.repositories.chat.ChatRepository
import uddug.com.domain.entities.chat.File as ChatFile
import java.io.File as JavaFile
import javax.inject.Inject

class ChatInteractor @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend fun getDialogs(folderId: Long? = null): List<Chat> = chatRepository.getChats(folderId)

    suspend fun getFolders(): List<ChatFolder> = chatRepository.getFolders()

    suspend fun createFolder(
        name: String,
        dialogIds: List<Long> = emptyList(),
        ord: Int? = null,
    ): ChatFolder = chatRepository.createFolder(name, dialogIds, ord)

    suspend fun updateFolder(
        folderId: Long,
        name: String? = null,
        dialogIds: List<Long>? = null,
        ord: Int? = null,
    ): ChatFolder = chatRepository.updateFolder(folderId, name, dialogIds, ord)

    suspend fun deleteFolder(folderId: Long) = chatRepository.deleteFolder(folderId)

    suspend fun getFolder(folderId: Long): ChatFolderDetails = chatRepository.getFolder(folderId)

    suspend fun getFolderDialogs(
        folderId: Long,
        limit: Int,
        page: Int,
    ): ChatFolderDialogsPage = chatRepository.getFolderDialogs(folderId, limit, page)

    suspend fun markFolderAsRead(folderId: Long) = chatRepository.markFolderAsRead(folderId)

    suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat> = chatRepository.getMessages(currentUserId, dialogId, limit, lastMessageId)

    suspend fun getDialogInfo(dialogId: Long): DialogInfo = chatRepository.getDialogInfo(dialogId)

    suspend fun getDialogInfoByPeer(interlocutorId: String): DialogInfo =
        chatRepository.getDialogInfoByPeer(interlocutorId)

    suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat> =
        chatRepository.getMessagesWithOwnerInfo(currentUserId, dialogId, limit, lastMessageId)

    suspend fun getDialogMedia(
        dialogId: Long,
        category: ChatMediaCategory,
        limit: Int,
        page: Int,
        query: String?,
        sd: String?,
        ed: String?,
    ): List<MediaMessage> =
        chatRepository.getDialogMedia(dialogId, category, limit, page, query, sd, ed)

    suspend fun searchMessages(
        currentUserId: String,
        dialogId: Long,
        searchField: String,
        limit: Int = 50,
        lastMessageId: Long? = null,
        sd: String? = null,
        ed: String? = null,
    ): List<MessageChat> =
        chatRepository.searchMessages(currentUserId, dialogId, searchField, limit, lastMessageId, sd, ed)

    suspend fun createDialog(dialogName: String?, userRoles: Map<String, String?>): Long =
        chatRepository.createDialog(dialogName = dialogName, userRoles = userRoles)

    suspend fun createGroupDialog(
        dialogName: String?,
        userRoles: Map<String, String?>,
        imageId: String? = null,
    ): Long =
        chatRepository.createGroupDialog(dialogName = dialogName, userRoles = userRoles, imageId = imageId)

    suspend fun updateDialogInfo(
        dialogId: Long,
        dialogName: String? = null,
        imageId: String? = null,
        removeImage: Boolean = false,
    ): DialogInfo =
        chatRepository.updateDialogInfo(dialogId, dialogName, imageId, removeImage)

    suspend fun updateGroupDialog(
        dialogId: Long,
        dialogName: String? = null,
        imageId: String? = null,
        removeImage: Boolean = false,
        users: List<String>? = null,
    ): DialogInfo =
        chatRepository.updateGroupDialog(dialogId, dialogName, imageId, removeImage, users)

    suspend fun searchUsers(query: String, limit: Int = 10, page: Int = 1): List<UserProfileFullInfo> =
        chatRepository.searchUsers(query, limit, page)

    suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, readStatus: Int) =
        chatRepository.markMessagesRead(dialogId, messages, readStatus)

    suspend fun pinDialog(dialogId: Long) = chatRepository.pinDialog(dialogId)

    suspend fun unpinDialog(dialogId: Long) = chatRepository.unpinDialog(dialogId)

    suspend fun pinMessage(dialogId: Long, messageId: Long) =
        chatRepository.pinMessage(dialogId, messageId)

    suspend fun unpinMessage(dialogId: Long, messageId: Long) =
        chatRepository.unpinMessage(dialogId, messageId)

    suspend fun updateMessage(
        dialogId: Long,
        messageId: Long,
        text: String,
        files: List<FileDescriptor> = emptyList(),
    ): MessageChat = chatRepository.updateMessage(dialogId, messageId, text, files)

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false) =
        chatRepository.deleteMessage(messageId, forMe)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false) =
        chatRepository.deleteMessages(messages, forMe)

    suspend fun uploadFiles(files: List<JavaFile>, raw: Boolean = false): List<ChatFile> =
        chatRepository.uploadFiles(files, raw)

    suspend fun deleteGroupDialog(dialogId: Long) = chatRepository.deleteGroupDialog(dialogId)

    suspend fun leaveGroupDialog(dialogId: Long) = chatRepository.leaveGroupDialog(dialogId)

    suspend fun makeDialogAdmin(dialogId: Long, userId: String) =
        chatRepository.makeDialogAdmin(dialogId, userId)

    suspend fun removeDialogAdmin(dialogId: Long, userId: String) =
        chatRepository.removeDialogAdmin(dialogId, userId)

    suspend fun getUsersStatus(userIds: List<String>): List<UserStatus> =
        chatRepository.getUsersStatus(userIds)

    suspend fun searchDialogs(query: String): List<SearchDialog> =
        chatRepository.searchDialogs(query)

    suspend fun searchMessages(
        query: String,
        lastMessageId: Long? = null,
    ): List<SearchMessage> = chatRepository.searchMessages(query, lastMessageId)

    suspend fun createPoll(
        subject: String,
        isAnonymous: Boolean,
        multipleAnswers: Boolean,
        isQuiz: Boolean,
        options: List<PollOptionInput>,
    ): Poll =
        chatRepository.createPoll(subject, isAnonymous, multipleAnswers, isQuiz, options)

    suspend fun stopPoll(pollId: String): Boolean = chatRepository.stopPoll(pollId)

    suspend fun deletePoll(pollId: String) = chatRepository.deletePoll(pollId)

    suspend fun getPoll(pollId: String): Poll = chatRepository.getPoll(pollId)

    suspend fun answerPoll(pollId: String, optionIds: List<String>): Poll =
        chatRepository.answerPoll(pollId, optionIds)

    suspend fun getPollAnswerUsers(
        pollId: String,
        optionId: String,
        limit: Int = 10,
        page: Int = 1,
    ): List<UserProfileFullInfo> = chatRepository.getPollAnswerUsers(pollId, optionId, limit, page)
}
