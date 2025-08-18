package uddug.com.domain.entities.chat

/**
 * Represents online status of a user.
 */
data class UserStatus(
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: String?
)
