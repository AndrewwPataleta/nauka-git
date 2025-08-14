package uddug.com.domain.entities.chat

data class MediaMessage(
    val messageId: Long,
    val dialogId: Long,
    val mediaType: Int,
    val file: MediaFile,
    val createdAt: String,
    val sender: SenderInfo  // переименовано "o" → "sender" для ясности
)

data class MediaFile(
    val id: String,
    val path: String,
    val fileKind: Int,
    val fileName: String,
    val fileType: Int,
    val contentType: String,
    // Опциональные поля (если их нет в JSON, будет null)
    val fileSize: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null
)

data class SenderInfo(
    val id: String,  // было "i" → теперь "id"
    val fullName: String  // было "fn" → теперь "fullName"
)