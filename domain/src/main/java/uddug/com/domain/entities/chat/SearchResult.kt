package uddug.com.domain.entities.chat

import java.time.Instant

/** Search result for dialog */
data class SearchDialog(
    val dialogId: Long,
    val dialogType: Int,
    val messageId: Long,
    val fullName: String,
    val image: String?,
    val createdAt: Instant,
)

/** Search result for message */
data class SearchMessage(
    val dialogId: Long,
    val messageId: Long,
    val fullName: String,
    val image: String?,
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: String?,
    val text: String?,
    val createdAt: Instant,
)
