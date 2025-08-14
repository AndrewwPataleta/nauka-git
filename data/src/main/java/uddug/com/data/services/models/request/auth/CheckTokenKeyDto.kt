package uddug.com.data.services.models.request.auth

import com.google.gson.annotations.SerializedName

data class CheckTokenKeyDto(
    @SerializedName("key")
    val key: String,
    @SerializedName("clientId")
    val clientId: String? = null,
    @SerializedName("tabId")
    val tabId: String? = null,
    @SerializedName("authSessionId")
    val authSessionId: String? = null,
    @SerializedName("password")
    val password: String? = null
)