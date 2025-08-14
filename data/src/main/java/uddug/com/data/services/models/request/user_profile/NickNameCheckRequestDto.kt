package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName

data class NickNameCheckRequestDto(
    @SerializedName("nickname")
    val nickname: String
)