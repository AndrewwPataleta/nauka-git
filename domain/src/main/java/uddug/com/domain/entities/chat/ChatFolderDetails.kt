package uddug.com.domain.entities.chat

data class ChatFolderDetails(
    val folder: ChatFolder,
    val dialogIds: List<Long>,
    val dialogs: List<ChatFolderDialogSummary>,
)

data class ChatFolderDialogSummary(
    val dialogId: Long,
    val name: String?,
    val fullName: String?,
    val nickname: String?,
    val dialogType: Int,
    val folderNames: List<String>,
    val imagePath: String?,
)

data class ChatFolderDialog(
    val dialog: SearchDialog,
    val folderNames: List<String>,
)

data class ChatFolderDialogsPage(
    val dialogs: List<ChatFolderDialog>,
)
