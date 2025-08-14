package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class AdditionalPropDto(
    @SerializedName("term") var term: String? = null,
    @SerializedName("code") var code: String? = null
)
