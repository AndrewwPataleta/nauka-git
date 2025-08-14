package uddug.com.domain.entities.country

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Country(
    val id: String? = null,
    val addressId: String? = null,
    val isSelected: Boolean = false,
    val sd: String? = null,
    val ed: String? = null,
    val status: Int? = null,
    val cls: Int? = null,
    val parentNum: Int? = null,
    val rObject: String? = null,
    val parentClsNum: String? = null,
    val type: Int? = null,
    val num: Int? = null,
    val lang: Int? = null,
    val code: String? = null,
    val term: String? = null,
    val dsc: String? = null,
    val uref: String? = null,
    var city: String? = null
) : Parcelable
