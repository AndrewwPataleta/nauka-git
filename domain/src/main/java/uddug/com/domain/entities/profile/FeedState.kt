package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedState(

     var id: String? = null,
     var subscribed: Boolean? = null

): Parcelable
