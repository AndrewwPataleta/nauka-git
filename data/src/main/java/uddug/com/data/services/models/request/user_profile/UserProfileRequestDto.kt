package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName

data class UserProfileRequestDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("firstName")
    val firstName: String? = null,
    @SerializedName("lastName")
    val lastName: String? = null,
    @SerializedName("middleName")
    val middleName: Any? = null,
    @SerializedName("nickname")
    val nickname: String? = null,
    @SerializedName("birthDate")
    val birthDate: Any? = null,
    @SerializedName("phone")
    val phone: String? = null
)
