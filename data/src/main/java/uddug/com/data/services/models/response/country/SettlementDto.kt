package uddug.com.data.services.models.response.country

import com.google.gson.annotations.SerializedName


data class SettlementDto(
    @SerializedName("city") var city: String? = null,
    @SerializedName("level") var level: String? = null,
    @SerializedName("region") var region: String? = null,
    @SerializedName("socrname") var socrname: String? = null,
    @SerializedName("territory") var territory: String? = null,
    @SerializedName("uref") var uref: String? = null,
)
