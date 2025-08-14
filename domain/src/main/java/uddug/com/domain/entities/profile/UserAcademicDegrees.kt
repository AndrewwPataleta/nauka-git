package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserAcademicDegrees(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var name: String? = null,
    var titleDate: String? = null,
    var uref: String? = null
) : Parcelable
