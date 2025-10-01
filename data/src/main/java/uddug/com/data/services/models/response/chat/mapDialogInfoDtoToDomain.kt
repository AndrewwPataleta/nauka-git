package uddug.com.data.services.models.response.chat

import uddug.com.domain.entities.chat.ActiveCall
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.File
import uddug.com.domain.entities.chat.User


fun mapDialogInfoDtoToDomain(dto: DialogInfoDto): DialogInfo {
    return DialogInfo(
        id = dto.id,
        name = dto.name,
        type = dto.type,
        interlocutor = dto.interlocutor?.let { mapUserDtoToDomain(it) },
        dialogImage = dto.dialogImage?.let { mapFileDtoToDomain(it) },
        users = dto.users?.mapNotNull { mapUserDtoToDomain(it) } ?: emptyList(),
        isDeleted = dto.isDeleted ?: false,
        firstMessageId = dto.firstMessageId,
        isPinned = dto.isPinned ?: false,
        isUnread = dto.isUnread ?: false,
        pinnedMessageId = dto.pinnedMessageId,
        activeCall = dto.activeCall?.let { mapActiveCallDtoToDomain(it) },
        permits = dto.permits ?: emptyList()
    )
}

private fun mapUserDtoToDomain(dto: UserDto): User? {
    return try {
        User(
            image = dto.image?.path.orEmpty(),
            fullName = dto.fullName ?: "",
            nickname = dto.nickname ?: "",
            userId = dto.userId ?: return null,
            role = dto.role ?: ""
        )
    } catch (e: Exception) {
        null
    }
}

private fun mapFileDtoToDomain(dto: FileDto): File? {
    return try {
        File(
            id = dto.id,
            path = dto.path,
            fileName = dto.fileName,
            contentType = dto.contentType,
            fileSize = dto.fileSize,
            fileType = dto.fileType,
            fileKind = dto.fileKind,
            duration = dto.duration,
            viewCount = dto.viewCount
        )
    } catch (e: Exception) {
        null
    }
}

private fun mapActiveCallDtoToDomain(dto: ActiveCallDto): ActiveCall? {
    return try {
        ActiveCall(
            id = dto.id,
            format = dto.format,
            type = dto.type
        )
    } catch (e: Exception) {
        null
    }
}
