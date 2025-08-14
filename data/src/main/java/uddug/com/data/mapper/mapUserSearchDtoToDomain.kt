package uddug.com.data.mapper

import uddug.com.data.services.models.response.chat.UserSearchDto
import uddug.com.domain.entities.chat.User

fun mapUserSearchDtoToDomain(dto: UserSearchDto): User {
    return User(
        image = dto.image?.path,
        fullName = dto.fullName,
        nickname = dto.nickname,
        userId = dto.id,
    )
}
