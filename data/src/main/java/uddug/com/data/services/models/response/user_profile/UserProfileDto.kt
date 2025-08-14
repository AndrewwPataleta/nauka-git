package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class UserProfileDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("middleName")
    val middleName: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("birthDate")
    val birthDate: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("placeOfResidence")
    val placeOfResidence: String?,
    @SerializedName("dsc")
    val descriptionProfile: String?,
    @SerializedName("grants")
    val grants: List<String>,
    @SerializedName("meta")
    val metaDto: MetaDto? = null
)
