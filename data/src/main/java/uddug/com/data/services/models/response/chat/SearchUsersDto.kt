package uddug.com.data.services.models.response.chat

import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto

data class SearchUsersDto(
    val users: List<UserProfileFullInfoDto>,
    val count: Int
)
