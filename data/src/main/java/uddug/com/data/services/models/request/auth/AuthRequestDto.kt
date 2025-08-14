package uddug.com.data.services.models.request.auth

import com.google.gson.annotations.SerializedName

data class AuthRequestDto(
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("socialLogin")
    val socialLogin: String? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("key")
    val key: String? = null
)