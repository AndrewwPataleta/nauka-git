package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UpdateGroupAdminRequestDto(
    @SerializedName("userId") val userId: String,
)
