package uddug.com.data.mapper

import uddug.com.data.services.models.response.chat.FolderDto
import uddug.com.domain.entities.chat.ChatFolder

fun mapFolderDtoToDomain(folderDto: FolderDto): ChatFolder = ChatFolder(
    id = folderDto.id,
    name = folderDto.name,
    ord = folderDto.ord,
    unreadCount = folderDto.unreadCount
)
