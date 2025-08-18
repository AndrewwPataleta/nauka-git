package uddug.com.domain.entities.chat

data class ChatSocketMessage(
    val dialog: Long,
    val cType: Int = 1,
    val text: String,
    val owner: String? = null,
    val files: List<FileDescriptor>? = null
)

data class FileDescriptor(
    val id: String,
    val fileType: Int
)
