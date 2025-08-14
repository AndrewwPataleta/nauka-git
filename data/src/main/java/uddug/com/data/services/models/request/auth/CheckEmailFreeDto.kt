package uddug.com.data.services.models.request.auth

import com.google.gson.annotations.SerializedName

data class CheckEmailFreeDto(
    @SerializedName("email")
    val email: String
)