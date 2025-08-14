package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class ContactsDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("up") var up: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("cType") var cType: String? = null,
    @SerializedName("cForm") var cForm: String? = null,
    @SerializedName("contact") var contact: String? = null,
    @SerializedName("cIsIdentified") var cIsIdentified: String? = null,
    @SerializedName("cLang") var cLang: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("uref") var uref: String? = null
)
