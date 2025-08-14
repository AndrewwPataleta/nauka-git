package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActivityAreasMap(
    var additionalProp1: String? = null,
    var additionalProp2: String? = null,
    var additionalProp3: String? = null
): Parcelable
