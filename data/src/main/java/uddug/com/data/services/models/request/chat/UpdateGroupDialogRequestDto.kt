package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UpdateGroupDialogRequestDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("image") val image: DialogImageRequestDto? = null,
    @SerializedName("users") val users: List<String>? = null,
)
