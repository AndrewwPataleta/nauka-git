package uddug.com.data.mapper


import uddug.com.data.repositories.chat.dto.FileDto
import uddug.com.data.repositories.chat.dto.MessageDto
import uddug.com.data.services.models.response.chat.MessagePollDto
import uddug.com.data.services.models.response.chat.MessagePollOptionDto
import uddug.com.domain.entities.chat.File
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.entities.chat.PollOption
import java.time.Instant


fun MessageDto.toDomain(currentUserId: String): MessageChat = MessageChat(
    id = id,
    text = text,
    type = MessageType.fromInt(type),
    files = files.map { it.toDomain() },
    ownerId = ownerId,
    createdAt = createdAt.toInstantOrEpoch(),
    readCount = read,
    isMine = ownerId == currentUserId,
    poll = poll?.toDomain(text, id)
)

private fun FileDto.toDomain(): File = File(
    id = id,
    path = path,
    fileName = fileName,
    contentType = contentType,
    fileSize = null,
    fileType = fileType,
    fileKind = fileKind,
    duration = null,
    viewCount = null,
)

fun MessagePollDto.toDomain(questionFallback: String?, messageId: Long): Poll {
    val optionDtos = options ?: shortOptions ?: emptyList()
    val sortedOptions = optionDtos.sortedBy { it.order ?: Int.MAX_VALUE }
    return Poll(
        id = id ?: shortId.orEmpty(),
        dialogId = dialogId ?: shortDialogId,
        messageId = messageId,
        subject = (subject ?: shortSubject ?: questionFallback).orEmpty(),
        isAnonymous = isAnonymous ?: shortIsAnonymous ?: false,
        multipleAnswers = multipleAnswers ?: shortMultipleAnswers ?: false,
        isQuiz = isQuiz ?: shortIsQuiz ?: false,
        isStopped = isStopped ?: shortIsStopped ?: false,
        options = sortedOptions.map { it.toDomain() },
    )
}

fun MessagePollOptionDto.toDomain(): PollOption = PollOption(
    id = id ?: shortId.orEmpty(),
    value = (value ?: shortValue).orEmpty(),
    description = description ?: shortDescription,
    isRightAnswer = isRightAnswer ?: shortIsRightAnswer,
    voteCount = voteCount ?: shortVoteCount ?: 0,
    isVoted = voted ?: shortVoted ?: false,
    answeredUsers = emptyList(),
)

private fun String.toInstantOrEpoch(): Instant =
    runCatching { Instant.parse(this) }.getOrElse { Instant.EPOCH }
