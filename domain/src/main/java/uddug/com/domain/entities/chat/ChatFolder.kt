package uddug.com.domain.entities.chat

/**
 * Represents a dialog folder grouping chat dialogs.
 */
data class ChatFolder(
    val id: Long,
    val name: String,
    val ord: Int,
    val unreadCount: Int
)
