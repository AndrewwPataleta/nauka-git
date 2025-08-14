package uddug.com.domain.entities.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Files(

    @SerializedName("id") var id: String? = null,
    @SerializedName("path") var path: String? = null,
    @SerializedName("fileName") var fileName: String? = null,
    @SerializedName("contentType") var contentType: String? = null,
    @SerializedName("fileSize") var fileSize: Int? = null,
    @SerializedName("fileKind") var fileKind: Int? = null,
    @SerializedName("fileType") var fileType: Int? = null

): Parcelable