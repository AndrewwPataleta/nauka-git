package uddug.com.domain.entities.chat

data class MediaMessage(
    val messageId: Long,
    val dialogId: Long,
    val mediaType: Int,
    val file: MediaFile,
    val createdAt: String,
    val sender: SenderInfo  
)

data class MediaFile(
    val id: String,
    val path: String,
    val fileKind: Int,
    val fileName: String,
    val fileType: Int,
    val contentType: String? = null,
    
    val fileSize: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null
)

data class SenderInfo(
    val id: String,  
    val fullName: String  
)
