package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserTitles(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var cTitle: String? = null,
    var rCertifyingDocuments: String? = null,
    var titleDate: String? = null,
    var uref: String? = null
): Parcelable
