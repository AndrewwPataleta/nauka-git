package uddug.com.data.services.models.response.chat

import uddug.com.data.repositories.chat.dto.FileDto
import uddug.com.domain.entities.chat.Attachment
import uddug.com.domain.entities.chat.File
import uddug.com.domain.entities.chat.FileKind
import uddug.com.domain.entities.chat.FileType
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.profile.Image
import java.time.Instant

// ChatDto.kt

data class ChatDto(
    val dialogs: List<DialogDto>,
    val sumUnreadMessages: Int,
    val count: Int,
)

data class DialogDto(
    val dialogId: Long,
    val dialogName: String,
    val dialogType: Int,
    val messageId: Long,
    val isPinned: Boolean,
    val isUnread: Boolean,
    val users: List<UserDto>,
    val interlocutor: UserDto,
    val lastMessage: MessageDto,
    val unreadMessages: Int,
    val notificationsDisable: Boolean,
    val isBlocked: Boolean = false,
)

data class UserDto(
    val image: ImageDto? = null,
    val fullName: String,
    val nickname: String,
    val userId: String,
    val role: String,
)

data class ImageDto(
    val id: String,
    val path: String? = null,
    val fileName: String,
    val contentType: String,
    val fileSize: Int,
    val fileType: Int,
    val fileKind: Int,
    val duration: String,
    val viewCount: Int,
) {
    fun toDomain() : File {
        return File(
            id = this.id,
            contentType = this.contentType,
            path = this.path.orEmpty(),
            fileName = this.fileName
        )
    }
}

data class MessageDto(
    val id: Long,
    val text: String,
    val type: Int,
    val files: List<ImageDto>,
    val read: Int,
    val ownerId: String,
    val createdAt: String,
    val isPinned: Boolean,
) {
    fun toDomain(currentUserId: String): MessageChat = MessageChat(

        id = id,
        text = text,
        type = MessageType.fromInt(type),
        files = files.map { it.toDomain() },
        ownerId = ownerId,
        createdAt = Instant.now(),
        readCount = read,
        isMine = ownerId == currentUserId
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

