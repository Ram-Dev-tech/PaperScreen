package com.paperscreen.android.reader.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DocumentManager {

    suspend fun importDocument(context: Context, uri: Uri): BookEntity? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var displayName = "Unknown Document"
            var size = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) displayName = cursor.getString(nameIndex)
                    
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
            
            // To ensure persistence without keeping SAF permissions forever (which can expire or be revoked),
            // it's safer to copy the document into our app's internal storage if it's not already there.
            // But since the user specifically said "The original file should remain accessible through the appropriate Android storage mechanism."
            // We should use `takePersistableUriPermission`.
            
            try {
                val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                // If it's already a local file or doesn't support persistable permissions
            }

            val fileType = when {
                displayName.endsWith(".txt", ignoreCase = true) -> "TXT"
                displayName.endsWith(".epub", ignoreCase = true) -> "EPUB"
                displayName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                else -> return@withContext null // Unsupported
            }

            // Clean title (remove extension)
            val title = displayName.substringBeforeLast(".")

            return@withContext BookEntity(
                uriString = uri.toString(),
                title = title,
                author = null, // Will be extracted later for EPUB
                fileType = fileType,
                coverUriString = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
