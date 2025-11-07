package uddug.com.domain.entities.chat

import com.google.gson.annotations.SerializedName

data class ChatSocketMessage(
    val dialog: Long? = null,
    val interlocutor: String? = null,
    val cType: Int = 1,
    val text: String? = null,
    val owner: String? = null,
    val files: List<FileDescriptor>? = null,
    val answered: Long? = null,
    val pollId: String? = null,
    val poll: ChatPoll? = null,
    val ansPreview: AnswerPreview? = null,
    val forwarded: Long? = null,
    val forwardedn: List<Long>? = null,
    val dialogs: List<Long>? = null,
    val id: Long? = null,
    val createdAt: String? = null,
    val ownerName: String? = null,
    val ownerAvatarUrl: String? = null,
    val read: Int? = null,
)

public data class FileDescriptor(
    val id: String,
    val fileType: Int,
    val path: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val fileSize: Int? = null,
    val fileKind: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null,
)

data class AnswerPreview(
    val i: Long,
    val mt: Int? = null,
    val o: PreviewOwner? = null,
    val t: String? = null,
    val f: PreviewFile? = null,
    val st: List<PreviewFileStat>? = null,
)

data class PreviewOwner(
    val i: String? = null,
    val fn: String? = null,
    val im: String? = null,
)

data class PreviewFile(
    val path: String? = null,
    val fileName: String? = null,
    val fileType: Int? = null,
)

data class PreviewFileStat(
    val c: Int? = null,
    val ft: Int? = null,
)

data class ChatPoll(
    @SerializedName("i") val id: String? = null,
    @SerializedName("s") val subject: String? = null,
    @SerializedName("a") val isAnonymous: Boolean? = null,
    @SerializedName("m") val multipleAnswers: Boolean? = null,
    @SerializedName("q") val isQuiz: Boolean? = null,
    @SerializedName("st") val isStopped: Boolean? = null,
    @SerializedName("oo") val options: List<ChatPollOption>? = null,
)

data class ChatPollOption(
    @SerializedName("i") val id: String? = null,
    @SerializedName("v") val value: String? = null,
    @SerializedName("dsc") val description: String? = null,
    @SerializedName("pv") val voteCount: Int? = null,
    @SerializedName("vd") val isVoted: Boolean? = null,
    @SerializedName("ord") val order: Int? = null,
)
