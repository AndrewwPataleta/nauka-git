package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import uddug.com.domain.entities.profile.Body


@Parcelize
data class FeedContainer(
    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("rFeed") var rFeed: String? = null,
    @SerializedName("upPost") var upPost: UpPost = UpPost(),
    @SerializedName("body") var body: Body? = Body(),
    @SerializedName("pubDate") var pubDate: String? = null,
    @SerializedName("cStatus") var cStatus: String? = null,
    @SerializedName("cSrcType") var cSrcType: String? = null,
    @SerializedName("rObject") var rObject: String? = null,
    @SerializedName("visible") var visible: Boolean? = null,
    @SerializedName("uref") var uref: String? = null
) : Parcelable