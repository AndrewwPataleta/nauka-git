package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import uddug.com.domain.entities.country.Country

@Parcelize
data class LaborActivities(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var rOrg: String? = null,
    var rOrgItem: ROrgItem? = ROrgItem(),
    var orgName: String? = null,
    var cPosition: String? = null,
    var position: String? = null,
    var startWork: String? = null,
    var endWork: String? = null,
    var dsc: String? = null,
    var cCountry: String? = null,
    var cCity: String? = null,
    var city: String? = null,
    var cityAsString: String? = null,
    var country: Country? = Country(),
    var activityAreasMap: Map<String, String> = mapOf(),
    var uref: String? = null,
    var cActivityAreas: List<String> = listOf()
) : Parcelable
