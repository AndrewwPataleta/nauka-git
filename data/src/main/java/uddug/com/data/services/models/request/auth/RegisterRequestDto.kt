package uddug.com.data.services.models.request.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null
)