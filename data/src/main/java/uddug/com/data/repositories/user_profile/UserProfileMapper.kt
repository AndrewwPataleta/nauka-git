package uddug.com.data.repositories.user_profile

import uddug.com.data.cache.user_id.UserIdCache
import uddug.com.data.services.models.request.user_profile.UserProfileRequestDto
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import toothpick.InjectConstructor

@InjectConstructor
class UserProfileMapper(
    private val userIdCache: UserIdCache,
) {

    fun mapDomainToDto(ui: ShortInfoUi) = ui.run {
        UserProfileRequestDto(
            id = userIdCache.entity,
            firstName = name,
            lastName = surname,
            middleName = if (middleName.isNullOrEmpty()) " " else middleName,
            // nick name should be more or equals 6 digits after id
            nickname = "id${nickname}"
        )
    }


}