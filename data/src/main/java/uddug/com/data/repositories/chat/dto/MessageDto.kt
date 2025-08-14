package uddug.com.data.repositories.chat.dto

import java.io.Serializable


data class MessageDto(
    val id: Long,
    val text: String? = null,
    val type: Int,
    val files: List<FileDto> = emptyList(),
    val ownerId: String? = null,
    val createdAt: String,
    val read: Int? = null,
)


data class FileDto(
    val id: String,
    val path: String,
    val fileKind: Int,
    val fileName: String,
    val fileType: Int,
    val contentType: String,
)
