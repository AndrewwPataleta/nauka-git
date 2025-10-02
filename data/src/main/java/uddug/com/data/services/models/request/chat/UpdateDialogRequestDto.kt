package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UpdateDialogRequestDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("image") val image: UpdateDialogImageRequestDto? = null,
    @SerializedName("users") val users: List<String>? = null,
)

data class UpdateDialogImageRequestDto(
    @SerializedName("id") val id: String,
    @SerializedName("fileType") val fileType: Int = 0,
)

