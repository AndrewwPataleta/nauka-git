package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName

data class UserProfileShortRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("middleName")
    val middleName: String? = null,
    @SerializedName("birthDate")
    val birthDate: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("dsc")
    val dsc: String? = null,
    @SerializedName("nickname")
    val nickname: String? = null,
)
