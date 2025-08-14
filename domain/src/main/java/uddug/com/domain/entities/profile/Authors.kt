package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Authors(
   var id: String? = null,
   var sd: String? = null,
   var ed: String? = null,
   var lastName: String? = null,
   var firstName: String? = null,
   var middleName: String? = null,
   var fioShort: String? = null,
   var engFioShort: String? = null,
   var dsc: String? = null,
   var engDsc: String? = null,
   var contacts: List<Contacts> = arrayListOf(),
   var identifiers: List<Identifiers> = arrayListOf(),
   var uref: String? = null
): Parcelable
