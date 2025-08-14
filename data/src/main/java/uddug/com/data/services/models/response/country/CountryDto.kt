package uddug.com.data.services.models.response.country

import com.google.gson.annotations.SerializedName



data class CountryDto(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("cls") var cls: Int? = null,
    @SerializedName("parentNum") var parentNum: Int? = null,
    @SerializedName("parentClsNum") var parentClsNum: String? = null,
    @SerializedName("type") var type: Int? = null,
    @SerializedName("num") var num: Int? = null,
    @SerializedName("lang") var lang: Int? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("term") var term: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("cityAsString") var city: String? = null

)
