package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UsersStatusRequestDto(
    @SerializedName("users") val users: List<String>
)
