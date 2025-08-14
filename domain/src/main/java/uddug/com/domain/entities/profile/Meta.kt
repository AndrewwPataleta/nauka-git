package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Meta(
    var subscnCount: Int? = null,
    var subscrCount: Int? = null
): Parcelable
