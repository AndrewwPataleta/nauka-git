package uddug.com.data.services.models.response.user_profile

import com.google.gson.annotations.SerializedName


data class AuthorsDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("lastName") var lastName: String? = null,
    @SerializedName("firstName") var firstName: String? = null,
    @SerializedName("middleName") var middleName: String? = null,
    @SerializedName("fioShort") var fioShort: String? = null,
    @SerializedName("engFioShort") var engFioShort: String? = null,
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("engDsc") var engDsc: String? = null,
    @SerializedName("contacts") var contacts: List<ContactsDto> = arrayListOf(),
    @SerializedName("identifiers") var identifiers: List<IdentifiersDto> = arrayListOf(),
    @SerializedName("uref") var uref: String? = null
)
