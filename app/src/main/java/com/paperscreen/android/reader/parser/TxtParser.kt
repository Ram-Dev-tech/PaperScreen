package com.paperscreen.android.reader.parser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object TxtParser {
    // Basic chunked reading for very large TXT files
    // For this simple implementation, we read chunks or lines.
    
    suspend fun readTextChunk(context: Context, uri: Uri, offset: Long, maxChars: Int): Pair<String, Long> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.skip(offset)
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val buffer = CharArray(maxChars)
                val charsRead = reader.read(buffer)
                
                if (charsRead == -1) {
                    return@withContext Pair("", offset)
                }
                
                return@withContext Pair(String(buffer, 0, charsRead), offset + charsRead)
            }
        } catch (e: Exception) {
            android.util.Log.e("TxtParser", "Failed to read chunk", e)
        }
        Pair("", offset)
    }

    suspend fun getTotalSize(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                return@withContext pfd.statSize
            }
        } catch (e: Exception) {
            android.util.Log.e("TxtParser", "Failed to get total size", e)
        }
        0L
    }
}
