package uddug.com.data.services.models.response.chat

import uddug.com.data.mapper.toDomain
import uddug.com.data.repositories.chat.dto.FileDto
import uddug.com.data.services.models.response.chat.MessagePollDto
import uddug.com.domain.entities.chat.Attachment
import uddug.com.domain.entities.chat.File
import uddug.com.domain.entities.chat.FileKind
import uddug.com.domain.entities.chat.FileType
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import java.time.Instant



data class ChatDto(
    val dialogs: List<DialogDto>,
    val sumUnreadMessages: Int,
    val count: Int,
)

data class DialogDto(
    val dialogId: Long,
    val dialogName: String,
    val dialogType: Int,
    val dialogImage: ImageDto? = null,
    val messageId: Long,
    val isPinned: Boolean,
    val isUnread: Boolean,
    val users: List<UserDto>?,
    val interlocutor: UserDto? = null,
    val lastMessage: MessageDto? = null,
    val unreadMessages: Int,
    val notificationsDisable: Boolean,
    val isBlocked: Boolean = false,
)

data class UserDto(
    val image: ImageDto? = null,
    val fullName: String? = null,
    val nickname: String? = null,
    val userId: String? = null,
    val role: String? = null,
    val isAdmin: Boolean? = null,
)

data class ImageDto(
    val id: String? = null,
    val path: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val fileSize: Int? = null,
    val fileType: Int? = null,
    val fileKind: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null,
) {
    fun toDomain(): File {
        return File(
            id = this.id.orEmpty(),
            contentType = this.contentType,
            path = this.path.orEmpty(),
            fileName = this.fileName.orEmpty()
        )
    }
}

data class MessageDto(
    val id: Long? = null,
    val text: String? = null,
    val type: Int? = null,
    val files: List<ImageDto>? = null,
    val read: Int? = null,
    val ownerId: String? = null,
    val createdAt: String? = null,
    val isPinned: Boolean? = null,
    val poll: MessagePollDto? = null,
) {
    fun toDomain(currentUserId: String): MessageChat = MessageChat(

        id = id ?: 0L,
        text = text,
        type = MessageType.fromInt(type ?: 0),
        files = files?.map { it.toDomain() } ?: emptyList(),
        ownerId = ownerId,
        createdAt = createdAt?.let { Instant.parse(it) } ?: Instant.EPOCH,
        readCount = read,
        isMine = ownerId == currentUserId,
        poll = poll?.toDomain(text, id ?: 0L)
    )

    fun FileDto.toDomain(): Attachment = Attachment(
        id = id,
        path = path,
        kind = FileKind.IMAGE,
        name = fileName,
        type = when (fileType) {
            1 -> FileType.IMAGE
            2 -> FileType.VIDEO
            else -> FileType.UNKNOWN
        },
        contentType = contentType
    )


}

