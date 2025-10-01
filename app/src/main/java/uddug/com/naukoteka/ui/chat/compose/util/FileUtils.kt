package uddug.com.naukoteka.ui.chat.compose.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        val displayName = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }

        val sanitizedName = displayName?.takeIf { it.isNotBlank() }
            ?.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            ?: "chat_attach_${System.currentTimeMillis()}"
        val targetFile = createUniqueFile(context.cacheDir, sanitizedName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: return null

        targetFile
    } catch (e: Exception) {
        null
    }
}

fun createUniqueFile(directory: File, fileName: String): File {
    val dotIndex = fileName.lastIndexOf('.')
    val baseName = when {
        dotIndex > 0 -> fileName.substring(0, dotIndex)
        else -> fileName
    }.ifBlank { "chat_attach" }
    val extension = when {
        dotIndex > 0 -> fileName.substring(dotIndex)
        else -> ""
    }

    var candidate = File(directory, baseName + extension)
    var index = 1
    while (candidate.exists()) {
        candidate = File(directory, "$baseName($index)$extension")
        index++
    }
    return candidate
}
