package uddug.com.data.services.models.response.user_profile
import com.google.gson.annotations.SerializedName


data class CheckNickNameResponseDto(
    @SerializedName("nicknameIsFree")
    val nicknameIsFree: Boolean
)