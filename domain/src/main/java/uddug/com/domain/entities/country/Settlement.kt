package uddug.com.domain.entities.country

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Settlement(
    val id: String? = null,
    val city: String? = null,
    val level: String? = null,
    val region: String? = null,
    val socrname: String? = null,
    val territory: String? = null,
    val uref: String? = null,
    val isSelected: Boolean = false
) : Parcelable

