package uddug.com.domain.entities.chat


import java.time.Instant

data class MessageChat(
    val id: Long,
    val text: String?,
    val type: MessageType,
    val files: List<File>,
    val ownerId: String?,
    val createdAt: Instant,
    val readCount: Int?,
    val ownerName: String? = null,
    val ownerAvatarUrl: String? = null,
    val ownerIsAdmin: Boolean = false,
    val isMine: Boolean,
    val replyTo: MessageChat? = null,
)

data class Attachment(
    val id: String,
    val path: String,
    val kind: FileKind,
    val name: String,
    val type: FileType,
    val contentType: String?
)

enum class MessageType {
    TEXT, SYSTEM, UNKNOWN;

    companion object {
        fun fromInt(value: Int): MessageType = when (value) {
            1 -> TEXT
            5 -> SYSTEM
            else -> UNKNOWN
        }
    }
}

enum class FileKind { IMAGE, OTHER }
enum class FileType { IMAGE, VIDEO, UNKNOWN }
