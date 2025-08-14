package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class MetaDto(
    @SerializedName("subscn_count") var subscnCount: Int? = null,
    @SerializedName("subscr_count") var subscrCount: Int? = null
)
