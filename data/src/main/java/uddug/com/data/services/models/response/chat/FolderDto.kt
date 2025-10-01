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

data class FolderDetailsDto(
    val id: Long,
    val name: String,
    val ord: Int,
    val unreadCount: Int,
    val dialogIds: List<Long>,
    val dialogs: List<SearchDialogDto>
)

data class FolderDialogsDto(
    val dialogs: List<FolderDialogItemDto>
)

data class FolderDialogItemDto(
    val dialog: SearchDialogDto,
    val folderNames: List<String>
)
