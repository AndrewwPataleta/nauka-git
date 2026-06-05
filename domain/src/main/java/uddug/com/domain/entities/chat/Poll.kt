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
    /**
     * Id of the poll author. Comes from the `o` field of the chat preview and
     * lets any client (even on a forwarded poll) gate author-only actions —
     * stopping the poll, opening the detailed results.
     */
    val authorId: String? = null,
)

data class PollOption(
    val id: String,
    val value: String,
    val description: String?,
    val isRightAnswer: Boolean?,
    val voteCount: Int,
    /**
     * Percentage of votes for this option, sent by the server as `pv` in the
     * chat socket/preview payload. Null if the client has to compute it from
     * [voteCount] (e.g. REST `/poll/:id` response).
     */
    val percent: Int?,
    val isVoted: Boolean,
    val answeredUsers: List<UserProfileFullInfo>,
)

data class PollOptionInput(
    val value: String,
    val isRightAnswer: Boolean? = null,
    val description: String? = null,
)
