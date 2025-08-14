package uddug.com.domain.entities.feed

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthorInfo (

  @SerializedName("rEntity"  ) var rEntity  : String? = null,
  @SerializedName("fullName" ) var fullName : String? = null,
  @SerializedName("imageUrl" ) var imageUrl : String? = null,
  @SerializedName("image"    ) var image    : String? = null

): Parcelable