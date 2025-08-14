package uddug.com.data.services.models.request.chat

import com.google.gson.annotations.SerializedName

data class CreateDialogRequest(
    @SerializedName("id")
    val id: Long
)
