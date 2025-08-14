package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AdditionalProp(
    var term: String? = null,
    var code: String? = null
): Parcelable
