package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName

data class ROrgItemDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("up") var up: String? = null,
    @SerializedName("cType") var cType: String? = null,
    @SerializedName("cStatus") var cStatus: String? = null,
    @SerializedName("cForm") var cForm: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("shortName") var shortName: String? = null,
    @SerializedName("altName") var altName: String? = null,
    @SerializedName("ogrn") var ogrn: Int? = null,
    @SerializedName("inn") var inn: Int? = null,
    @SerializedName("kpp") var kpp: Int? = null,
    @SerializedName("regDate") var regDate: String? = null,
    @SerializedName("ctOkpo") var ctOkpo: String? = null,
    @SerializedName("cIndustrySubmission") var cIndustrySubmission: String? = null,
    @SerializedName("cIsVerified") var cIsVerified: String? = null,
    @SerializedName("logo") var logoDto: LogoDto? = LogoDto(),
    @SerializedName("addressData") var addressDatumDtos: ArrayList<AddressDataDto> = arrayListOf(),
    @SerializedName("legalAddress") var legalAddressDto: LegalAddressDto? = LegalAddressDto(),
    @SerializedName("activityAreas") var activityAreaDtos: ArrayList<ActivityAreasDto> = arrayListOf(),
    @SerializedName("uref") var uref: String? = null

)
