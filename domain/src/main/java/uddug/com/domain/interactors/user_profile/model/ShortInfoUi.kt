package uddug.com.domain.interactors.user_profile.model

data class ShortInfoUi(
    val surname: String,
    val name: String,
    val nickname: String,
    val middleName: String? = null,
)