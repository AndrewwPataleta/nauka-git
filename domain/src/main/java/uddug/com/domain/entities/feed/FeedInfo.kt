package uddug.com.domain.entities.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedInfo (

  @SerializedName("feedId"     ) var feedId     : String? = null,
  @SerializedName("feedItemId" ) var feedItemId : String? = null,
  @SerializedName("rFeedOwner" ) var rFeedOwner : String? = null,
  @SerializedName("feedTitle"  ) var feedTitle  : String? = null

): Parcelable