package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UpdateMessageRequestDto(
    @SerializedName("dialogId") val dialogId: Long,
    @SerializedName("messageId") val messageId: Long,
    @SerializedName("updatedText") val updatedText: String?,
    @SerializedName("files") val files: List<String> = emptyList(),
)
