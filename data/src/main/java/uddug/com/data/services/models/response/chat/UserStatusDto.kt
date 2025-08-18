package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName

/**
 * DTO for user online status response.
 */
data class UserStatusDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("status") val status: StatusDto
)

data class StatusDto(
    @SerializedName("isOnline") val isOnline: Boolean,
    @SerializedName("lastSeen") val lastSeen: String?
)
