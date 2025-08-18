package uddug.com.data.mapper

import uddug.com.data.services.models.response.chat.UserStatusDto
import uddug.com.domain.entities.chat.UserStatus

fun mapUserStatusDtoToDomain(dto: UserStatusDto): UserStatus {
    return UserStatus(
        userId = dto.userId,
        isOnline = dto.status.isOnline,
        lastSeen = dto.status.lastSeen
    )
}
