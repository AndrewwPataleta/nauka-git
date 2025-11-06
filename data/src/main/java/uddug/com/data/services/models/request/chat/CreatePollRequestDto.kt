package uddug.com.data.services.models.request.chat

data class CreatePollRequestDto(
    val subject: String,
    val isAnonymous: Boolean,
    val multipleAnswers: Boolean,
    val isQuiz: Boolean,
    val options: List<PollOptionRequestDto>,
)

data class PollOptionRequestDto(
    val value: String,
    val isRightAnswer: Boolean? = null,
    val dsc: String? = null,
)
