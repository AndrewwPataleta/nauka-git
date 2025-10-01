package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName


data class SearchDialogDto(
    @SerializedName("dialogId") val dialogId: Long,
    @SerializedName("dialogType") val dialogType: Int,
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("image") val image: ImageDto?,
    @SerializedName("createdAt") val createdAt: String,
)
