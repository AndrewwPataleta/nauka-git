package uddug.com.data.services.models.response.file

data class UploadFileDto(
    val id: String,
    val path: String,
    val fileName: String,
    val contentType: String,
    val fileSize: Long,
    val fileKind: Int,
    val fileType: Int?
)
