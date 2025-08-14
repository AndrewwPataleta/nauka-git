package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NewPasswordDataRequest(
     var confirmation: String? = null,
     var currentPassword: String? = null,
     var newPassword: String? = null,
): Parcelable
