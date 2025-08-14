package uddug.com.domain.entities.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Identifiers(
    var id: String? = null,
    var sd: String? = null,
    var ed: String? = null,
    var cIdentSystem: String? = null,
    var cIdentSystemItem: IdentSystemItem? = null,
    var rObject: String? = null,
    var identifier: String? = null,
    var uref: String? = null
) : Parcelable

@Parcelize
data class IdentSystemItem(
    var id: Int? = null,
    var sd: String? = null,
    var ed: String? = null,
    var cIdentSystem: String? = null,
    var cIdentSystemItem: String? = null,
    var rObject: String? = null,
    var identifier: String? = null,
    var uref: String? = null
): Parcelable
