package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BlockUser(
    val id: String? = null,
    val name: String
) : Parcelable
