package uddug.com.data.services.models.response.auth

import com.google.gson.annotations.SerializedName

data class SocialTypesResponseDto(
    @SerializedName("idpVariants")
    val idpVariants: List<String>
)