package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName

/**
 * Lightweight poll payload that can be embedded into chat message responses.
 */
data class MessagePollDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("i") val shortId: String? = null,
    @SerializedName("dialogId") val dialogId: Long? = null,
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
    // Per backend docs: "aa" = array of user-selected option IDs (on poll level,
    // not option level). Used to mark which options current user voted for.
    @SerializedName("aa") val answeredOptionIds: List<String>? = null,
)

data class MessagePollOptionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("i") val shortId: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("v") val shortValue: String? = null,
    @SerializedName("description") val description: String? = null,
    // Per backend docs, description short key is "d" (not "dsc").
    @SerializedName("d") val shortDescription: String? = null,
    @SerializedName("isRightAnswer") val isRightAnswer: Boolean? = null,
    // Per backend docs, isRightAnswer short key is "r" (not "ra").
    @SerializedName("r") val shortIsRightAnswer: Boolean? = null,
    @SerializedName("voteCount") val voteCount: Int? = null,
    // Per backend docs, "pv" is percentValue (percentage), NOT vote count.
    @SerializedName("pv") val shortPercent: Int? = null,
    @SerializedName("voted") val voted: Boolean? = null,
    @SerializedName("ord") val order: Int? = null,
)
