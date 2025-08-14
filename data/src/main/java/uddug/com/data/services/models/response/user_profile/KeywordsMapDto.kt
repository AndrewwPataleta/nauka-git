package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class KeywordsMapDto(
    @SerializedName("additionalProp1") var additionalPropDto: AdditionalPropDto? = AdditionalPropDto(),
    @SerializedName("additionalProp2") var additionalPropDto2: AdditionalPropDto? = AdditionalPropDto(),
    @SerializedName("additionalProp3") var additionalPropDto3: AdditionalPropDto? = AdditionalPropDto()
)
