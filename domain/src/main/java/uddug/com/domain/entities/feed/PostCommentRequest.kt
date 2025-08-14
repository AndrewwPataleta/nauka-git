package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostCommentRequest(

    @SerializedName("asc") var asc: Boolean,
    @SerializedName("pageSize") var pageSize: Int,
    @SerializedName("postId") var postId: String,
    @SerializedName("view") var view: String? = null,

    ) : Parcelable