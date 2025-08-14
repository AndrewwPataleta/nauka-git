package uddug.com.naukoteka.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import uddug.com.domain.repositories.auth.models.SocialType

object IntentKeys {

    @Parcelize
    data class Registration(val key: String) : Parcelable {
        companion object {
            const val KEY = "registration"
        }
    }
    @Parcelize
    data class RecoveryPassword(val key: String) : Parcelable {
        companion object {
            const val KEY = "recovery"
        }
    }

    @Parcelize
    data class SocialAuthorization(val key: String, val socialType: SocialType) : Parcelable {
        companion object {
            const val KEY = "social-auth"
        }
    }


}