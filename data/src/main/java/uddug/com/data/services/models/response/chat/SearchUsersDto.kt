package uddug.com.data.services.models.response.chat

import uddug.com.data.services.models.response.chat.ImageDto

// DTO for users search endpoint

data class SearchUsersDto(
    val users: List<UserSearchDto>,
    val count: Int,
)

data class UserSearchDto(
    val id: String,
    val fullName: String?,
    val nickname: String?,
    val permits: List<String>?,
    val image: ImageDto?,
)
