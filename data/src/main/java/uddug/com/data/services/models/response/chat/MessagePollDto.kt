package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName

/**
 * Lightweight poll payload that can be embedded into chat message responses.
 */
data class MessagePollDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("i") val shortId: String? = null,
    @SerializedName("dialogId") val dialogId: Long? = null,
    @SerializedName("d") val shortDialogId: Long? = null,
    @SerializedName("messageId") val messageId: Long? = null,
    @SerializedName("mid") val shortMessageId: Long? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("s") val shortSubject: String? = null,
    @SerializedName("isAnonymous") val isAnonymous: Boolean? = null,
    @SerializedName("a") val shortIsAnonymous: Boolean? = null,
    @SerializedName("multipleAnswers") val multipleAnswers: Boolean? = null,
    @SerializedName("m") val shortMultipleAnswers: Boolean? = null,
    @SerializedName("isQuiz") val isQuiz: Boolean? = null,
    @SerializedName("q") val shortIsQuiz: Boolean? = null,
    @SerializedName("isStopped") val isStopped: Boolean? = null,
    @SerializedName("st") val shortIsStopped: Boolean? = null,
    @SerializedName("options") val options: List<MessagePollOptionDto>? = null,
    @SerializedName("oo") val shortOptions: List<MessagePollOptionDto>? = null,
)

data class MessagePollOptionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("i") val shortId: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("v") val shortValue: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("dsc") val shortDescription: String? = null,
    @SerializedName("isRightAnswer") val isRightAnswer: Boolean? = null,
    @SerializedName("ra") val shortIsRightAnswer: Boolean? = null,
    @SerializedName("voteCount") val voteCount: Int? = null,
    @SerializedName("pv") val shortVoteCount: Int? = null,
    @SerializedName("voted") val voted: Boolean? = null,
    @SerializedName("vd") val shortVoted: Boolean? = null,
    @SerializedName("ord") val order: Int? = null,
)
