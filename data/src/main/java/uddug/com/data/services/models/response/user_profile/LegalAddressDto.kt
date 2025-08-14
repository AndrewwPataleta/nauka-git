package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class LegalAddressDto(

    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("cType") var cType: String? = null,
    @SerializedName("postalcode") var postalcode: Int? = null,
    @SerializedName("ctFias") var ctFias: String? = null,
    @SerializedName("room") var room: String? = null,
    @SerializedName("notStucturedAddress") var notStucturedAddress: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("cIsVerified") var cIsVerified: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("cCountry") var cCountry: String? = null,
    @SerializedName("cCity") var cCity: String? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("cityAsString") var cityAsString: String? = null,
    @SerializedName("uref") var uref: String? = null

)
