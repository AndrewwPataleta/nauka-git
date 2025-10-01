package uddug.com.data.services.models.request.chat

data class ChatFolderRequestDto(
    val name: String? = null,
    val dialogIds: List<Long>? = null,
    val ord: Int? = null,
)
