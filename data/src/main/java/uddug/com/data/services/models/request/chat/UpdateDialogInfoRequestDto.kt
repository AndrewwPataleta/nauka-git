package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class UpdateDialogInfoRequestDto(
    @SerializedName("dialogName") val dialogName: String? = null,
    @SerializedName("dialogImage") val dialogImage: DialogImageRequestDto? = null,
    @SerializedName("removeDialogImage") val removeDialogImage: Boolean? = null,
)
