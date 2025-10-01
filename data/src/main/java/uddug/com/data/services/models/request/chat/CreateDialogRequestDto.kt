package uddug.com.data.services.models.request.chat

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class CreateDialogRequestDto(
    @SerializedName("dialogName") val dialogName: String?,
    @SerializedName("dialogImage") val dialogImage: DialogImageRequestDto?,
    @SerializedName("userRoles") val userRoles: JsonObject
)

data class DialogImageRequestDto(
    @SerializedName("id") val id: String,
    @SerializedName("fileType") val fileType: Int = 0,
)
