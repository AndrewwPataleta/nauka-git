package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class DialogUserActionRequestDto(
    @SerializedName("userId") val userId: String,
)

