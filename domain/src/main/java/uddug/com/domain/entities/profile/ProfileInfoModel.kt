package uddug.com.domain.entities.profile

data class ProfileInfoModel(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val imageUrl: String? = null,
    val currentJob: String? = null,
    val currentJobTitle: String? = null,
    val descriptionProfile: String? = null,
    val workExperiences: List<WorkExperienceModel> = emptyList(),
    val placeOfResidence: LocationModel? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class WorkExperienceModel(
    val id: String,
    val jobTitle: String,
    val jobPlace: String,
)

data class LocationModel(
    val name: String,
)
