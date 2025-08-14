package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName
import uddug.com.data.services.models.response.country.CountryDto
import uddug.com.data.services.models.response.user_profile.ROrgItemDto

data class AddUserAcademicDegreesDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("titleDate") var titleDate: String? = null
)
