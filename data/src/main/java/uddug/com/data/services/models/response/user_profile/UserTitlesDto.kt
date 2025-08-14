package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class UserTitlesDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("cTitle") var cTitle: String? = null,
    @SerializedName("rCertifyingDocuments") var rCertifyingDocuments: String? = null,
    @SerializedName("titleDate") var titleDate: String? = null,
    @SerializedName("uref") var uref: String? = null
)
