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

/**
 * Poll preview embedded in a cType=9 chat message. Field names follow the
 * chat poll preview spec (короткие ключи ради оптимизации трафика).
 */
data class ChatPoll(
    @SerializedName("i") val id: String? = null,
    @SerializedName("s") val subject: String? = null,
    /** `a` — опрос активен (true) или завершён (false). */
    @SerializedName("a") val isActive: Boolean? = null,
    @SerializedName("m") val multipleAnswers: Boolean? = null,
    @SerializedName("q") val isQuiz: Boolean? = null,
    /** `o` — автор опроса (заполнено только вложенное поле `i`). */
    @SerializedName("o") val author: ChatPollAuthor? = null,
    /** `aa` — id вариантов, за которые проголосовал текущий пользователь. */
    @SerializedName("aa") val answeredOptionIds: List<String>? = null,
    @SerializedName("oo") val options: List<ChatPollOption>? = null,
)

data class ChatPollAuthor(
    @SerializedName("i") val id: String? = null,
)

data class ChatPollOption(
    @SerializedName("i") val id: String? = null,
    @SerializedName("v") val value: String? = null,
    /** `d` — описание правильного варианта, приходит только для завершённого опроса. */
    @SerializedName(value = "d", alternate = ["dsc"]) val description: String? = null,
    /** `pv` — процент проголосовавших за вариант (0..100), уже округлён сервером. */
    @SerializedName("pv") val percent: Int? = null,
    /** `r` — признак правильного варианта, приходит только для завершённой викторины. */
    @SerializedName("r") val isRightAnswer: Boolean? = null,
    @SerializedName("ord") val order: Int? = null,
)
