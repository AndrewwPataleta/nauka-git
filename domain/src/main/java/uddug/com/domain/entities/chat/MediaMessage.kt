package uddug.com.domain.entities.chat

import com.google.gson.annotations.SerializedName

data class MediaMessage(
    val messageId: Long,
    val dialogId: Long,
    val mediaType: Int,
    val file: MediaFile? = null,
    val createdAt: String? = null,
    // Gson десериализует MediaMessage напрямую из ответа API и игнорирует
    // Kotlin non-null типы: если поле отсутствует в JSON (напр. у голосовых
    // сообщений), оно будет null. Поэтому sender обязан быть nullable.
    val sender: SenderInfo? = null,
    val text: String? = null,
    val linkPreview: LinkPreview? = null,
)

data class MediaFile(
    val id: String,
    val path: String,
    val fileKind: Int,
    val fileName: String,
    val fileType: Int,
    val contentType: String? = null,

    val fileSize: Int? = null,
    val duration: String? = null,
    val viewCount: Int? = null
)

data class SenderInfo(
    val id: String? = null,
    val fullName: String? = null
)

data class LinkPreview(
    @SerializedName("og:url")   val url: String? = null,
    @SerializedName("og:title") val title: String? = null,
    @SerializedName("og:image") val image: String? = null,
    @SerializedName("og:type")  val type: String? = null,
)
