package uddug.com.data.services.models.response.chat

import com.google.gson.annotations.SerializedName

/**
 * Preview представления сообщения. Используется, в частности, для поля
 * [DialogInfoDto.pinnedMessages].
 *
 * Сервер возвращает компактные ключи (i, o, t, mt, f, st, l, cd, ud) — парсим
 * их через [SerializedName] и маппим на нормальные имена полей.
 */
data class MessagePreviewDto(
    @SerializedName("i") val id: Long,
    @SerializedName("o") val owner: MessageOwnerPreviewDto? = null,
    @SerializedName("t") val text: String? = null,
    @SerializedName("mt") val messageType: Int? = null,
    @SerializedName("f") val files: List<FileDto>? = null,
    @SerializedName("cd") val createdAt: String? = null,
    @SerializedName("ud") val updatedAt: String? = null,
)

data class MessageOwnerPreviewDto(
    @SerializedName("userId", alternate = ["i", "id"]) val userId: String? = null,
    @SerializedName("fullName", alternate = ["fn", "n"]) val fullName: String? = null,
    @SerializedName("nickname", alternate = ["nick"]) val nickname: String? = null,
    @SerializedName("image", alternate = ["img"]) val image: FileDto? = null,
)
