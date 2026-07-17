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
    val poll: Poll? = null,
    val forwardedFromName: String? = null,
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
    TEXT, SYSTEM, POLL, VOICE, UNKNOWN;

    companion object {
        // Системные сообщения о звонках: 3 - начался, 6 - завершён,
        // 6003 - "Вызов пропущен", 6004 - "Звонок пропущен". Их создаёт сервер,
        // отображаются по центру, а не баблом.
        fun fromInt(value: Int): MessageType = when (value) {
            1 -> TEXT
            2, 3, 5, 6, 6003, 6004 -> SYSTEM
            4 -> VOICE
            9 -> POLL
            else -> UNKNOWN
        }
    }
}

enum class FileKind { IMAGE, OTHER }
enum class FileType { IMAGE, VIDEO, UNKNOWN }
