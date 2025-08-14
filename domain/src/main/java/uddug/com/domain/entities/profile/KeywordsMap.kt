package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class KeywordsMap(
     var additionalProp: AdditionalProp? = AdditionalProp(),
     var additionalProp2: AdditionalProp? = AdditionalProp(),
     var additionalProp3: AdditionalProp? = AdditionalProp()
): Parcelable
