package com.nauchat.core.ext

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.getFileName(resolver: ContentResolver): String {
    val cursor = resolver.query(this, null, null, null, null)
    var name = ""
    cursor?.let {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        name = cursor.getString(nameIndex)
        cursor.close()
    }
    return name
}
