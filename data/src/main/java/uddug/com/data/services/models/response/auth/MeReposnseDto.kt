package uddug.com.data.services.models.response.auth
import com.google.gson.annotations.SerializedName

data class MeReposnseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("emailVerified")
    val emailVerified: Boolean,
    @SerializedName("providerId")
    val providerId: String
)
