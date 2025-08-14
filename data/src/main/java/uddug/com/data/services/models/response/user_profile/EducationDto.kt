package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName
import uddug.com.data.services.models.response.country.CountryDto


data class EducationDto(
  @SerializedName("id") var id: String? = null,
  @SerializedName("sd") var sd: String? = null,
  @SerializedName("ed") var ed: String? = null,
  @SerializedName("rUser") var rUser: String? = null,
  @SerializedName("rOrg") var rOrg: String? = null,
  @SerializedName("rOrgItem") var rOrgItemDto: ROrgItemDto? = ROrgItemDto(),
  @SerializedName("cType") var cType: String? = null,
  @SerializedName("cLevel") var cLevel: String? = null,
  @SerializedName("cLevelItem") var cLevelItem: LevelItem? = LevelItem(),
  @SerializedName("specialty") var specialty: String? = null,
  @SerializedName("qualification") var qualification: String? = null,
  @SerializedName("rCertifyingDocuments") var rCertifyingDocuments: String? = null,
  @SerializedName("dsc") var dsc: String? = null,
  @SerializedName("name") var name: String? = null,
  @SerializedName("department") var department: String? = null,
  @SerializedName("startDate") var startDate: String? = null,
  @SerializedName("endDate") var endDate: String? = null,
  @SerializedName("cCountry") var cCountry: String? = null,
  @SerializedName("cCountryItem") var country: CountryDto? = null,
  @SerializedName("cCity") var cCity: String? = null,
  @SerializedName("city") var city: String? = null,
  @SerializedName("cityAsString") var cityAsString: String? = null,
  @SerializedName("uref") var uref: String? = null,
  @SerializedName("orgName") var orgName: String? = null
)


data class CountyItem(
  @SerializedName("id") var id: String? = null,
  @SerializedName("term") var term: String? = null,
)

data class LevelItem(
  @SerializedName("id") var id: String? = null,
  @SerializedName("term") var term: String? = null,
)
