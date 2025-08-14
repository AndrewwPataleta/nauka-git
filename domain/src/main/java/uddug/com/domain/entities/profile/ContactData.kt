package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactData(
     var id: String? = null,
     var sd: String? = null,
     var ed: String? = null,
     var up: String? = null,
     var rObject: String? = null,
     var cType: String? = null,
     var cForm: String? = null,
     var contact: String? = null,
     var cIsIdentified: String? = null,
     var cLang: String? = null,
     var dsc: String? = null,
     var uref: String? = null
): Parcelable
