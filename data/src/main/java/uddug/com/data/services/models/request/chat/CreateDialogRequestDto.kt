package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

/**
 * Request body for creating a dialog in chat service.
 */
data class CreateDialogRequestDto(
    @SerializedName("dialogName")
    val dialogName: String,
    @SerializedName("dialogImage")
    val dialogImage: DialogImageDto? = null,
    @SerializedName("userRoles")
    val userRoles: Map<String, String>,
)

/**
 * Represents dialog image information for create dialog request.
 */
data class DialogImageDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("fileType")
    val fileType: Int,
)
