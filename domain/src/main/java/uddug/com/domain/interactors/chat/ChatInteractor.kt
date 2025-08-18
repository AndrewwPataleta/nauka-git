package uddug.com.domain.interactors.chat

import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.repositories.chat.ChatRepository
import javax.inject.Inject

class ChatInteractor @Inject constructor(
    private val chatRepository: ChatRepository,
) {

    suspend fun getDialogs(): List<Chat> = chatRepository.getChats()

    suspend fun getMessages(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat> = chatRepository.getMessages(currentUserId, dialogId, limit, lastMessageId)

    suspend fun getDialogInfo(dialogId: Long): DialogInfo = chatRepository.getDialogInfo(dialogId)

    suspend fun getMessagesWithOwnerInfo(
        currentUserId: String,
        dialogId: Long,
        limit: Int = 50,
        lastMessageId: Long? = null,
    ): List<MessageChat> =
        chatRepository.getMessagesWithOwnerInfo(currentUserId, dialogId, limit, lastMessageId)

    suspend fun getDialogMedia(
        dialogId: Long, category: Int,
        limit: Int,
        page: Int,
        query: String?,
        sd: String?,
        ed: String?,
    ): List<MediaMessage> =
        chatRepository.getDialogMedia(dialogId, category, limit, page, query, sd, ed)

    suspend fun createDialog(userId: Long): Long = chatRepository.createDialog(userId)

    suspend fun markMessagesRead(dialogId: Long, messages: List<Long>, status: Int) =
        chatRepository.markMessagesRead(dialogId, messages, status)

    suspend fun updateMessage(messageId: Long, text: String): MessageChat =
        chatRepository.updateMessage(messageId, text)

    suspend fun deleteMessage(messageId: Long, forMe: Boolean = false) =
        chatRepository.deleteMessage(messageId, forMe)

    suspend fun deleteMessages(messages: List<Long>, forMe: Boolean = false) =
        chatRepository.deleteMessages(messages, forMe)
}
