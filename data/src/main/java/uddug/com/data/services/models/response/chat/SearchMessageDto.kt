package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName


data class SearchMessageDto(
    @SerializedName("dialogId") val dialogId: Long,
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("image") val image: ImageDto?,
    @SerializedName("userId") val userId: String,
    @SerializedName("status") val status: StatusDto,
    @SerializedName("text") val text: String?,
    @SerializedName("createdAt") val createdAt: String,
)
