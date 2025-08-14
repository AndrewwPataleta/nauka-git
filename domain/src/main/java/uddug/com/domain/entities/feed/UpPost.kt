package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import uddug.com.domain.entities.profile.CStatusItem
import uddug.com.domain.entities.profile.FeedInfo
import uddug.com.domain.entities.profile.FeedOwnerInfo

@Parcelize
data class UpPost(

    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("publicationDate") var publicationDate: String? = null,
    @SerializedName("rAuthor") var rAuthor: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("cCommentStatus") var cCommentStatus: String? = null,
    @SerializedName("cStatus") var cStatus: String? = null,
    @SerializedName("cStatusItem") var cStatusItem: CStatusItem? = CStatusItem(),
    @SerializedName("feedId") var feedId: String? = null,
    @SerializedName("feedInfo") var feedInfo: FeedInfo? = FeedInfo(),
    @SerializedName("meta") var meta: Meta? = Meta(),
    @SerializedName("authorInfo") var authorInfo: AuthorInfo? = AuthorInfo(),
    @SerializedName("feedOwnerInfo") var feedOwnerInfo: FeedOwnerInfo? = FeedOwnerInfo(),
    @SerializedName("permits") var permits: ArrayList<String> = arrayListOf(),
    @SerializedName("commentNtfStatus") var commentNtfStatus: Boolean? = null,
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("text") var text: String? = null,
    @SerializedName("rAuthorId") var rAuthorId: String? = null

) : Parcelable