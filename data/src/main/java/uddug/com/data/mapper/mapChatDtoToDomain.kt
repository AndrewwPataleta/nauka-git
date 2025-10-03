package uddug.com.data.mapper

import uddug.com.data.services.models.response.chat.ChatDto
import uddug.com.data.services.models.response.chat.ImageDto
import uddug.com.data.services.models.response.chat.MessageDto
import uddug.com.data.services.models.response.chat.UserDto
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.Image
import uddug.com.domain.entities.chat.Message
import uddug.com.domain.entities.chat.User

fun mapChatDtoToDomain(chatDto: ChatDto): List<Chat> {
    return chatDto.dialogs.map { dialog ->
        Chat(
            dialogId = dialog?.dialogId ?: 0,
            dialogName = dialog?.dialogName.orEmpty(),
            dialogType = dialog?.dialogType ?: 0,
            dialogImage = dialog?.dialogImage?.let { mapImageDtoToDomain(it) },
            messageId = dialog?.messageId ?: 0,
            isPinned = dialog?.isPinned ?: false,
            isUnread = dialog?.isUnread ?: false,
            users = dialog?.users?.map { mapUserDtoToDomain(it) } ?: emptyList(),
            interlocutor = mapUserDtoToDomain(dialog?.interlocutor ?: UserDto()),
            lastMessage = mapMessageDtoToDomain(dialog?.lastMessage),
            unreadMessages = dialog?.unreadMessages ?: 0,
            notificationsDisable = dialog?.notificationsDisable ?: false,
            isBlocked = dialog?.isBlocked ?: false
        )
    }
}

fun mapUserDtoToDomain(userDto: UserDto): User {
    return User(
        image = userDto.image?.path,
        fullName = userDto.fullName,
        nickname = userDto.nickname,
        userId = userDto.userId,
        role = userDto.role,
        isAdmin = userDto.isAdmin ?: false,
    )
}

fun mapImageDtoToDomain(imageDto: ImageDto): Image {
    return Image(
        id = imageDto.id,
        path = imageDto.path ?: "",
        fileName = imageDto.fileName,
        contentType = imageDto.contentType,
        fileSize = imageDto.fileSize,
        fileType = imageDto.fileType,
        fileKind = imageDto.fileKind,
        duration = imageDto.duration,
    )
}

fun mapMessageDtoToDomain(messageDto: MessageDto?): Message {
    return Message(
        id = messageDto?.id,
        text = messageDto?.text,
        type = messageDto?.type,
        files = messageDto?.files?.map { mapImageDtoToDomain(it) } ?: emptyList(),
        read = messageDto?.read,
        ownerId = messageDto?.ownerId,
        createdAt = messageDto?.createdAt,
        isPinned = messageDto?.isPinned
    )
}
