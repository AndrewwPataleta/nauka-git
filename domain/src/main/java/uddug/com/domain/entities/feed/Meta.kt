package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Meta(

    @SerializedName("first_comment_count") var firstCommentCount: Int? = null,
    @SerializedName("comment_count") var commentCount: Int? = null,
    @SerializedName("last_modified") var lastModified: String? = null,
    @SerializedName("view_count") var viewCount: Int? = null,
    @SerializedName("subscn_count") var subscnCount: Int? = null,
    @SerializedName("subscr_count") var subscrCount: Int? = null

) : Parcelable