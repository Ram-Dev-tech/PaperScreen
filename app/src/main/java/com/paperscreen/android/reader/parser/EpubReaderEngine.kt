package com.paperscreen.android.reader.parser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.asset.FileAsset
import org.readium.r2.shared.publication.services.locatorService
import org.readium.r2.streamer.Streamer
import org.readium.r2.streamer.parser.epub.EpubParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EpubReaderEngine(private val context: Context, private val uri: Uri) {

    private var publication: Publication? = null
    private var streamer: Streamer? = null
    
    // We must copy the URI to a local file because Readium FileAsset requires a File
    private var localEpubFile: File? = null

    suspend fun open(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Readium requires a File for FileAsset, so we copy the SAF uri to a temp file
            val tempFile = File(context.cacheDir, "temp_epub_${System.currentTimeMillis()}.epub")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            localEpubFile = tempFile

            val asset = FileAsset(tempFile)
            
            streamer = Streamer(context, parsers = listOf(EpubParser()))
            val result = streamer?.open(asset, allowUserInteraction = false)

            result?.onSuccess { pub ->
                publication = pub
            }
            
            return@withContext publication != null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun getChapterContent(index: Int): String? = withContext(Dispatchers.IO) {
        val pub = publication ?: return@withContext null
        val links = pub.readingOrder
        if (index < 0 || index >= links.size) return@withContext null

        val link = links[index]
        val resource = pub.get(link)
        
        resource.readAsString().getOrNull()
    }

    fun getLocatorForChapter(index: Int): String? {
        val pub = publication ?: return null
        val links = pub.readingOrder
        if (index < 0 || index >= links.size) return null
        val link = links[index]
        val locator = org.readium.r2.shared.publication.Locator(
            href = link.href,
            type = link.type ?: "application/xhtml+xml",
            title = link.title,
            locations = org.readium.r2.shared.publication.Locator.Locations(progression = 0.0)
        )
        return locator.toJSON().toString()
    }

    fun getChapterIndexFromLocator(json: String): Int {
        val pub = publication ?: return 0
        try {
            val locator = org.readium.r2.shared.publication.Locator.fromJSON(org.json.JSONObject(json))
            if (locator != null) {
                val index = pub.readingOrder.indexOfFirst { it.href == locator.href }
                if (index != -1) return index
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    fun getChapterTitle(index: Int): String? {
        val pub = publication ?: return null
        val links = pub.readingOrder
        if (index < 0 || index >= links.size) return null
        return links[index].title
    }

    fun getChapterCount(): Int {
        return publication?.readingOrder?.size ?: 0
    }

    fun getTitle(): String? {
        return publication?.metadata?.title
    }
    
    fun getAuthor(): String? {
        return publication?.metadata?.authors?.firstOrNull()?.name
    }

    fun close() {
        publication?.close()
        localEpubFile?.delete()
    }
}
