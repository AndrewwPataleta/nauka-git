package uddug.com.domain.repositories.models

import com.google.gson.annotations.SerializedName

data class UserAcademicDegrees(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("titleDate") var titleDate: String? = null
)
