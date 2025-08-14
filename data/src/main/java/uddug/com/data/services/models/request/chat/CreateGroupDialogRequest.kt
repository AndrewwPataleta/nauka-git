package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

/**
 * Request body for creating group dialog containing list of participant ids.
 */
data class CreateGroupDialogRequest(
    @SerializedName("ids")
    val ids: List<Long>
)
