package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName
import uddug.com.data.services.models.response.country.CountryDto
import uddug.com.data.services.models.response.user_profile.ROrgItemDto

data class UpdateUserAuthorInfoDto(
    @SerializedName("identifier") var identifier: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("cIdentSystem") var cIdentSystem: String? = null,
)
