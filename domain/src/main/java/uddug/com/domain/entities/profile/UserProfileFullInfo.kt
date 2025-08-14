package uddug.com.domain.entities.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import uddug.com.domain.entities.feed.Meta
import java.io.Serializable

@Parcelize
data class UserProfileFullInfo(
    var id: String? = null,
    var userAcademicDegree: List<UserAcademicDegrees> = listOf(),
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var firstNameEng: String? = null,
    var lastNameEng: String? = null,
    var nickname: String? = null,
    var email: String? = null,
    var birthDate: String? = null,
    var gender: String? = null,
    var phone: String? = null,
    var phone2: String? = null,
    var phone3: String? = null,
    var bannerUrl: String? = null,
    var grants: List<String> = listOf(),
    var image: Image? = Image(),
    var keywords: List<String>? = listOf(),
    var keywordsMap: KeywordsMap? = KeywordsMap(),
    var contactDatum: List<ContactData> = listOf(),
    var education: List<Education> = listOf(),
    var userTitle: List<UserTitles> = listOf(),
    var laborActivity: List<LaborActivities> = listOf(),
    var authors: List<Authors> = listOf(),
    var feedState: FeedState? = FeedState(),
    var dsc: String? = null,
    var permits: List<String> = listOf(),
    var uref: String? = null,
    var fullName: String? = null,
    var placeOfResidence: String? = null,
    var laborActivityCurrentPosition: LaborActivityCurrentPosition? = LaborActivityCurrentPosition(),
    var addresses: List<Addresses> = listOf(),
    var meta: Meta? = Meta(),
    var settings: @RawValue MutableMap<String, String> = mutableMapOf()
) : Parcelable, Serializable
