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

/**
 * Derives the user UUID to open the "Чужой профиль" screen.
 * Prefers [rAuthorId] (raw user id) when available; otherwise parses the UUID
 * out of [rEntity] which is a "N:UUID" ref (takes the substring after ':').
 * Returns null when nothing usable is present.
 */
fun AuthorInfo?.resolveUserId(rAuthorId: String? = null): String? {
    val fromAuthorId = rAuthorId?.takeIf { it.isNotBlank() }
    if (fromAuthorId != null) return fromAuthorId
    val ref = this?.rEntity?.takeIf { it.isNotBlank() } ?: return null
    val uuid = ref.substringAfter(':', ref)
    return uuid.takeIf { it.isNotBlank() }
}