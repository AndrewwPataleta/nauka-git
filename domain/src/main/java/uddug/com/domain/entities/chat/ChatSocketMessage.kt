package uddug.com.domain.entities.chat

data class ChatSocketMessage(
    val dialog: Long? = null,
    val interlocutor: String? = null,
    val cType: Int = 1,
    val text: String,
    val owner: String? = null,
    val files: List<FileDescriptor>? = null,
    val answered: Long? = null,
    val ansPreview: AnswerPreview? = null,
    val forwarded: Long? = null,
    val forwardedn: List<Long>? = null,
    val dialogs: List<Long>? = null,
)

public data class FileDescriptor(
    val id: String,
    val fileType: Int
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
