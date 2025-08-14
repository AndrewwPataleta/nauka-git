package uddug.com.data.cache.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class UserSystemSettings(
    val theme: UserTheme = UserTheme.LIGHT,
    val compressImage: Boolean = false,
    val compressVideo: Boolean = false,
    val autoPlayGif: Boolean = false,
    val autoPlayVideo: Boolean = false
) : Parcelable, Serializable

@Parcelize
enum class UserTheme : Parcelable, Serializable {
    LIGHT,
    DARK
}
