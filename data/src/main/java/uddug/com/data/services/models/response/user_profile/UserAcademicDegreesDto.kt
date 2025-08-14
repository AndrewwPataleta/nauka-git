package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class UserAcademicDegreesDto(

    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("titleDate") var titleDate: String? = null,
    @SerializedName("uref") var uref: String? = null

)
