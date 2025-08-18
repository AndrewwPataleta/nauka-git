package uddug.com.data.services.models.response.chat

// DialogInfoDto.kt
data class DialogInfoDto(
    val id: Long,
    val name: String?,
    val type: Int,
    val interlocutor: UserDto?,
    val dialogImage: FileDto?,
    val users: List<UserDto>,
    val isDeleted: Boolean,
    val firstMessageId: Long?,
    val isPinned: Boolean,
    val isUnread: Boolean,
    val pinnedMessageId: Long?,
    val activeCall: ActiveCallDto?,
    val permits: List<String>
)


data class FileDto(
    val id: String,
    val path: String,
    val fileName: String,
    val contentType: String? = null,
    val fileSize: Int,
    val fileType: Int,
    val fileKind: Int,
    val duration: String?,
    val viewCount: Int
)

data class ActiveCallDto(
    val id: Long,
    val format: Int,
    val type: Int
)