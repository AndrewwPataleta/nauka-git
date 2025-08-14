package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Logo(

     var id: String? = null,
     var path: String? = null,
     var fileName: String? = null,
     var contentType: String? = null,
     var fileSize: Int? = null,
     var fileKind: Int? = null,
     var fileType: Int? = null

): Parcelable
