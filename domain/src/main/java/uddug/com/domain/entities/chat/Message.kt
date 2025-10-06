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
    val cType: Int = 1,
    val contact: ChatContact? = null,
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
    TEXT, SYSTEM, CONTACT, UNKNOWN;

    companion object {
        fun fromInt(value: Int): MessageType = when (value) {
            1 -> TEXT
            5 -> SYSTEM
            7 -> CONTACT
            else -> UNKNOWN
        }
    }
}

data class ChatContact(
    val id: String? = null,
    val fullName: String? = null,
    val nickname: String? = null,
    val phone: String? = null,
    val image: String? = null,
) {
    val displayName: String
        get() = fullName?.takeIf { it.isNotBlank() }
            ?: nickname?.takeIf { it.isNotBlank() }
            ?: phone.orEmpty()

    val subtitle: String?
        get() = when {
            !phone.isNullOrBlank() -> phone
            !nickname.isNullOrBlank() -> nickname
            else -> null
        }

    companion object {
        fun fromPayload(payload: String?): ChatContact? {
            if (payload.isNullOrBlank()) return null
            return runCatching {
                val json = org.json.JSONObject(payload)
                ChatContact(
                    id = json.optString("id").takeIf { it.isNotBlank() },
                    fullName = json.optString("fullName")
                        .takeIf { it.isNotBlank() }
                        ?: json.optString("name").takeIf { it.isNotBlank() },
                    nickname = json.optString("nickname").takeIf { it.isNotBlank() },
                    phone = json.optString("phone").takeIf { it.isNotBlank() },
                    image = json.optString("image").takeIf { it.isNotBlank() },
                )
            }.getOrNull()
        }
    }
}

enum class FileKind { IMAGE, OTHER }
enum class FileType { IMAGE, VIDEO, UNKNOWN }
