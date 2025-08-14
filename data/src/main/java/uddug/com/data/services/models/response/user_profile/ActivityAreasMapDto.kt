package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class ActivityAreasMapDto(

    @SerializedName("additionalProp1") var additionalProp1: String? = null,
    @SerializedName("additionalProp2") var additionalProp2: String? = null,
    @SerializedName("additionalProp3") var additionalProp3: String? = null

)
