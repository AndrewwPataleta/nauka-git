package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ROrgItem(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var up: String? = null,
    var cType: String? = null,
    var cStatus: String? = null,
    var cForm: String? = null,
    var dsc: String? = null,
    var name: String? = null,
    var shortName: String? = null,
    var altName: String? = null,
    var ogrn: Int? = null,
    var inn: Int? = null,
    var kpp: Int? = null,
    var regDate: String? = null,
    var ctOkpo: String? = null,
    var cIndustrySubmission: String? = null,
    var cIsVerified: String? = null,
    var logo: Logo? = Logo(),
    var addressDatum: List<AddressData> = arrayListOf(),
    var legalAddress: LegalAddress? = LegalAddress(),
    var activityArea: List<ActivityAreas> = arrayListOf(),
    var uref: String? = null
): Parcelable
