package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostCommentAddRequest(
    @SerializedName("rAuthor") var rAuthor: String? = null,
    @SerializedName("rPost") var rPost: String? = null,
    @SerializedName("cStatus") var cStatus: String? = null,
    @SerializedName("text") var text: String? = null

) : Parcelable