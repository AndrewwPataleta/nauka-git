package uddug.com.domain.entities.chat

data class ChatFolderDetails(
    val folder: ChatFolder,
    val dialogIds: List<Long>,
    val dialogs: List<SearchDialog>,
)

data class ChatFolderDialog(
    val dialog: SearchDialog,
    val folderNames: List<String>,
)

data class ChatFolderDialogsPage(
    val dialogs: List<ChatFolderDialog>,
)
