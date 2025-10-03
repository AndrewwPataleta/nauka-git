package uddug.com.domain.entities.chat




data class ChatFolder(
    val id: Long,
    val name: String,
    val ord: Int,
    val unreadCount: Int
)
