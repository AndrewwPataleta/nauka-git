package uddug.com.naukoteka.presentation.profile.edit.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class CountryType : Parcelable {
    BORN,
    LIVE
}

@Parcelize
enum class SettlementType : Parcelable {
    BORN,
    LIVE
}