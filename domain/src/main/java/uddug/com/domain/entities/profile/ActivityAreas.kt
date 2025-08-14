package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActivityAreas(
    var id: Int? = null,
    var sd: String? = null,
    var ed: String? = null,
    var status: Int? = null,
    var cls: Int? = null,
    var parentNum: Int? = null,
    var parentClsNum: String? = null,
    var type: Int? = null,
    var num: Int? = null,
    var lang: Int? = null,
    var code: String? = null,
    var term: String? = null,
    var dsc: String? = null,
    var uref: String? = null

) : Parcelable
