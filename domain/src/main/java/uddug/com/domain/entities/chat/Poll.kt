package uddug.com.domain.entities.chat

import uddug.com.domain.entities.profile.UserProfileFullInfo

data class Poll(
    val id: String,
    val dialogId: Long?,
    val messageId: Long?,
    val subject: String,
    val isAnonymous: Boolean,
    val multipleAnswers: Boolean,
    val isQuiz: Boolean,
    val isStopped: Boolean,
    val options: List<PollOption>,
)

data class PollOption(
    val id: String,
    val value: String,
    val description: String?,
    val isRightAnswer: Boolean?,
    val voteCount: Int,
    val isVoted: Boolean,
    val answeredUsers: List<UserProfileFullInfo>,
)

data class PollOptionInput(
    val value: String,
    val isRightAnswer: Boolean? = null,
    val description: String? = null,
)
