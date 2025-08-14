package uddug.com.data.services.models.response.auth

import com.google.gson.annotations.SerializedName

data class CheckTokenKeyResponseDto(
    @SerializedName("tokenType")
    val tokenType: String? = null,
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("tabId")
    val tabId: String? = null,
    @SerializedName("authSessionId")
    val authSessionId: String? = null,
    @SerializedName("followUp")
    val followUp: Boolean
)