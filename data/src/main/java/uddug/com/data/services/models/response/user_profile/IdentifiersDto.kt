package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class IdentifiersDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("cIdentSystem") var cIdentSystem: String? = null,
    @SerializedName("cIdentSystemItem") var cIdentSystemItem: IdentSystemItemDto? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("identifier") var identifier: String? = null,
    @SerializedName("uref") var uref: String? = null
)

data class IdentSystemItemDto(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("cIdentSystem") var cIdentSystem: String? = null,
    @SerializedName("cIdentSystemItem") var cIdentSystemItem: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("identifier") var identifier: String? = null,
    @SerializedName("uref") var uref: String? = null
)
