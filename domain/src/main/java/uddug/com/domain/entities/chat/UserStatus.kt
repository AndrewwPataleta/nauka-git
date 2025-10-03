package uddug.com.domain.entities.chat

data class UserStatus(
    val userId: String,
    val isOnline: Boolean,
    val lastSeen: String?
)
