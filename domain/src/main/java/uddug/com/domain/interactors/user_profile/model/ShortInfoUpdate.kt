package uddug.com.domain.interactors.user_profile.model

data class ShortInfoUpdate(
    val id: String,
    val name: String? = null,
    val surname: String? = null,
    val middleName: String? = null,
    val description: String? = null,
    val birthday: String? = null,
    val nickname: String? = null,
    val gender: String? = null,
)