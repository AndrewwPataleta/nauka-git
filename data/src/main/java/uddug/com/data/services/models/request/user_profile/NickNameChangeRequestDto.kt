package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName

data class NickNameChangeRequestDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("nickname")
    val nickname: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String
)