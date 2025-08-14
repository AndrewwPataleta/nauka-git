package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LaborActivityCurrentPosition(
     var position: String? = null,
     var cPosition: String? = null,
     var orgName: String? = null,
     var rOrg: String? = null
): Parcelable
