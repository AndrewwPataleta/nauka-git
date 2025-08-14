package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class FeedStateDto(

    @SerializedName("id") var id: String? = null,
    @SerializedName("subscribed") var subscribed: Boolean? = null

)
