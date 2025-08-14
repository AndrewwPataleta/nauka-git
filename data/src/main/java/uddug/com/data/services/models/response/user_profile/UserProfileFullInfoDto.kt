package uddug.com.data.services.models.response.user_profile

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


data class UserProfileFullInfoDto(
    @SerializedName("id") var id: String? = null,
    @SerializedName("userAcademicDegrees") var userAcademicDegreeDtos: ArrayList<UserAcademicDegreesDto> = arrayListOf(),
    @SerializedName("firstName") var firstName: String? = null,
    @SerializedName("middleName") var middleName: String? = null,
    @SerializedName("lastName") var lastName: String? = null,
    @SerializedName("firstNameEng") var firstNameEng: String? = null,
    @SerializedName("lastNameEng") var lastNameEng: String? = null,
    @SerializedName("nickname") var nickname: String? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("birthDate") var birthDate: String? = null,
    @SerializedName("gender") var gender: String? = null,
    @SerializedName("phone") var phone: String? = null,
    @SerializedName("phone2") var phone2: String? = null,
    @SerializedName("phone3") var phone3: String? = null,
    @SerializedName("bannerUrl") var bannerUrl: String? = null,
    @SerializedName("grants") var grants: ArrayList<String> = arrayListOf(),
    @SerializedName("image") var imageDto: ImageDto? = ImageDto(),
    @SerializedName("keywords") var keywords: ArrayList<String> = arrayListOf(),
    @SerializedName("keywordsMap") var keywordsMapDto: KeywordsMapDto? = KeywordsMapDto(),
    @SerializedName("contactData") var contactDatumDtos: ArrayList<ContactDataDto> = arrayListOf(),
    @SerializedName("education") var educationDto: ArrayList<EducationDto> = arrayListOf(),
    @SerializedName("userTitles") var userTitleDtos: ArrayList<UserTitlesDto> = arrayListOf(),
    @SerializedName("laborActivities") var laborActivityDtos: ArrayList<LaborActivitiesDto> = arrayListOf(),
    @SerializedName("authors") var authors: ArrayList<AuthorsDto> = arrayListOf(),
    @SerializedName("feedState") var feedStateDto: FeedStateDto? = FeedStateDto(),
    @SerializedName("dsc") var dsc: String? = null,
    @SerializedName("permits") var permits: ArrayList<String> = arrayListOf(),
    @SerializedName("uref") var uref: String? = null,
    @SerializedName("fullName") var fullName: String? = null,
    @SerializedName("placeOfResidence") var placeOfResidence: String? = null,
    @SerializedName("laborActivityCurrentPosition") var laborActivityCurrentPositionDto: LaborActivityCurrentPositionDto? = LaborActivityCurrentPositionDto(),
    @SerializedName("addresses") var addresses: ArrayList<AddressesDto> = arrayListOf(),
    @SerializedName("meta") var metaDto: MetaDto? = MetaDto(),
    @SerializedName("settings") var settings: MutableMap<String, String> = mutableMapOf()
)
