package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import uddug.com.domain.entities.country.Country

@Parcelize
data class Addresses(

    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var cType: String? = null,
    var postalcode: Int? = null,
    var ctFias: String? = null,
    var room: String? = null,
    var notStucturedAddress: String? = null,
    var dsc: String? = null,
    var cIsVerified: String? = null,
    var rObject: String? = null,
    var cCountry: String? = null,
    var country: Country? = null,
    var cCity: String? = null,
    var city: String? = null,
    var cityAsString: String? = null,
    var uref: String? = null

) : Parcelable
