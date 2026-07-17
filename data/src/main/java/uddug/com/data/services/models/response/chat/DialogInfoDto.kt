package uddug.com.data.services.models.response.chat


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
    val pinnedMessages: List<MessagePreviewDto>? = null,
    val activeCall: ActiveCallDto?,
    val permits: List<String>
)


data class FileDto(
    val id: String,
    val path: String,
    val fileName: String,
    val contentType: String? = null,
    val fileSize: Int? = null,
    val fileType: Int? = null,
    val fileKind: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null,
)

data class ActiveCallDto(
    val id: Long,
    val format: Int,
    val type: Int
)
