package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostComment(

    @SerializedName("id") var id: String? = null,
    @SerializedName("sd") var sd: String? = null,
    @SerializedName("ed") var ed: String? = null,
    @SerializedName("up") var up: String? = null,
    @SerializedName("publicationDate") var publicationDate: String? = null,
    @SerializedName("rAuthor") var rAuthor: String? = null,
    @SerializedName("rPost") var rPost: String? = null,
    @SerializedName("cStatus") var cStatus: String? = null,
    @SerializedName("files") var files: ArrayList<String> = arrayListOf(),
    @SerializedName("meta") var meta: String? = null,
    @SerializedName("myReaction") var myReaction: String? = null,
    @SerializedName("authorInfo") var authorInfo: AuthorInfo? = AuthorInfo(),
    @SerializedName("rAuthorId") var rAuthorId: String? = null,
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("text") var text: String? = null

) : Parcelable