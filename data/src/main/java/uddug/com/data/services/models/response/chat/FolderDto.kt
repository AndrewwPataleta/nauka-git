package uddug.com.data.services.models.response.chat

data class FoldersDto(
    val userId: String,
    val folders: List<FolderDto>
)

data class FolderDto(
    val id: Long,
    val name: String,
    val ord: Int,
    val unreadCount: Int
)
