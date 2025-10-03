package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName
import uddug.com.data.services.models.response.country.CountryDto
import uddug.com.data.services.models.response.user_profile.ROrgItemDto

data class UpdateUserProfileLaborDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("rOrg") var rOrg: String? = null,
    
    @SerializedName("orgName") var orgName: String? = null,
    @SerializedName("cPosition") var cPosition: String? = null,
    @SerializedName("position") var position: String? = null,
    @SerializedName("startWork") var startWork: String? = null,
    @SerializedName("endWork") var endWork: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("cCountry") var cCountry: String? = null,
    @SerializedName("cCity") var cCity: String? = null,
    @SerializedName("city") var city: String? = null,
    @SerializedName("cityAsString") var cityAsString: String? = null,
    @SerializedName("activityAreasMap") var activityAreasMapDto: Map<String, String> = mapOf(),
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("cActivityAreas") var cActivityAreas: ArrayList<String> = arrayListOf()
)
