package uddug.com.data.services.models.response.chat

import uddug.com.data.services.models.response.user_profile.UserProfileFullInfoDto
import uddug.com.data.utils.toDomain
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.entities.chat.PollOption

data class PollDto(
    val id: String? = null,
    val dialogId: Long? = null,
    val messageId: Long? = null,
    val subject: String? = null,
    val isAnonymous: Boolean? = null,
    val multipleAnswers: Boolean? = null,
    val isQuiz: Boolean? = null,
    val isStopped: Boolean? = null,
    val options: List<PollOptionDto>? = null,
)

data class PollOptionDto(
    val id: String? = null,
    val value: String? = null,
    val dsc: String? = null,
    val isRightAnswer: Boolean? = null,
    val voteCount: Int? = null,
    val voted: Boolean? = null,
    val answeredUsers: List<UserProfileFullInfoDto>? = null,
)

fun PollDto.toDomain(): Poll = Poll(
    id = id.orEmpty(),
    dialogId = dialogId,
    messageId = messageId,
    subject = subject.orEmpty(),
    isAnonymous = isAnonymous ?: false,
    multipleAnswers = multipleAnswers ?: false,
    isQuiz = isQuiz ?: false,
    isStopped = isStopped ?: false,
    options = options.orEmpty().map { it.toDomain() },
)

fun PollOptionDto.toDomain(): PollOption = PollOption(
    id = id.orEmpty(),
    value = value.orEmpty(),
    description = dsc,
    isRightAnswer = isRightAnswer,
    voteCount = voteCount ?: 0,
    isVoted = voted ?: false,
    answeredUsers = answeredUsers.orEmpty().map { it.toDomain() },
)
