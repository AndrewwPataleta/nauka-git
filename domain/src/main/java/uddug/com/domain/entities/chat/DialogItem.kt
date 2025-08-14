//package uddug.com.domain.entities.chat
//
//data class DialogItem(
//    val dialogId: Int,
//    val dialogType: Int,
//    val messageId: Int,
//    val mediaType: Int,
//    val fullName: String,
//    val image: Image,
//    val userId: String,
//    val status: Status,
//    val text: String,
//    val file: File,
//    val linkPreview: LinkPreview,
//    val createdAt: String,
//    val folderNames: List<String>
//)
//
//data class Image(
//    val id: String,
//    val path: String,
//    val fileName: String,
//    val contentType: String,
//    val fileSize: Int,
//    val fileType: Int,
//    val fileKind: Int,
//    val duration: String,
//    val viewCount: Int
//)
//
//data class File(
//    val id: String,
//    val path: String,
//    val fileName: String,
//    val contentType: String,
//    val fileSize: Int,
//    val fileType: Int,
//    val fileKind: Int,
//    val duration: String,
//    val viewCount: Int
//)
//
//// Пустые классы для status и linkPreview, так как в JSON они представлены как {}
//data class Status(
//    // Добавьте поля, если они известны
//)
//
//data class LinkPreview(
//    // Добавьте поля, если они известны
//)