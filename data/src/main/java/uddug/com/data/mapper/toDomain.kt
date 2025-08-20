package uddug.com.data.mapper


import uddug.com.data.repositories.chat.dto.FileDto
import uddug.com.data.repositories.chat.dto.MessageDto
import uddug.com.domain.entities.chat.Attachment
import uddug.com.domain.entities.chat.FileKind
import uddug.com.domain.entities.chat.FileType
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import java.time.Instant

fun MessageDto.toDomain(currentUserId: String): MessageChat = MessageChat(
    id = id,
    text = text,
    type = MessageType.fromInt(type),
    files = files.map { it.toDomain() },
    ownerId = ownerId,
    createdAt = Instant.parse(createdAt),
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
