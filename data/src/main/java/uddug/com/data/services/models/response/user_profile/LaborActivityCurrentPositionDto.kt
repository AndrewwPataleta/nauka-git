package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class LaborActivityCurrentPositionDto(
    @SerializedName("position") var position: String? = null,
    @SerializedName("cPosition") var cPosition: String? = null,
    @SerializedName("orgName") var orgName: String? = null,
    @SerializedName("rOrg") var rOrg: String? = null
)
