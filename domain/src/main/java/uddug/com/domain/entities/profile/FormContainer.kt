package uddug.com.domain.entities.profile

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.json.JSONObject
import java.io.Serializable

@Parcelize
data class FormContainer(
    @SerializedName("name") var name: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("parentNum") var parentNum: String? = null,
    @SerializedName("num") var num: Int? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("elements") var elements: List<SettingsElement>? = null
) : Serializable, Parcelable

@Parcelize
data class SettingsElement(
    @SerializedName("type") var type: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("title") var title: String? = null,
    @SerializedName("cGroup") var cGroup: String? = null,
    @SerializedName("parentNum") var parentNum: String? = null,
    @SerializedName("num") var num: Int? = null,
    @SerializedName("cls") var cls: Int? = null,
    @SerializedName("clsParentNum") var clsParentNum: Int? = null,
    @SerializedName("formNum") var formNum: Int? = null,
    @SerializedName("context") var context: ArrayList<String> = arrayListOf(),
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("default") var default: @RawValue JsonElement? = null,
    @SerializedName("defaultUref") var defaultUref: String? = null,
    @SerializedName("ord") var ord: Int? = null
) : Serializable, Parcelable