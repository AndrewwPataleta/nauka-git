package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import uddug.com.domain.entities.country.Country

@Parcelize
data class Education(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var rUser: String? = null,
    var rOrg: String? = null,
    
    var cType: String? = null,
    var cLevel: String? = null,
    var cLevelName: String? = null,
    var specialty: String? = null,
    var qualification: String? = null,
    var rCertifyingDocuments: String? = null,
    var country: Country? = Country(),
    var dsc: String? = null,
    var name: String? = null,
    var department: String? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var cCity: String? = null,
    var city: String? = null,
    var cityAsString: String? = null,
    var uref: String? = null,
    var orgName: String? = null
) : Parcelable
