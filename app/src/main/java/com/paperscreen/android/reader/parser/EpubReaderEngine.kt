package com.paperscreen.android.reader.parser

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.epub.EpubParser
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import org.json.JSONObject

data class EpubTocItem(val title: String, val chapterIndex: Int)

class EpubReaderEngine(private val context: Context, private val uri: Uri) {

    private var publication: Publication? = null
    
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

            val httpClient = DefaultHttpClient()
            val assetRetriever = AssetRetriever(
                contentResolver = context.contentResolver, 
                httpClient = httpClient
            )

            val publicationOpener = PublicationOpener(
                publicationParser = EpubParser() // Use EpubParser directly
            )

            val url = AbsoluteUrl(tempFile.absolutePath) ?: return@withContext false
            val asset = assetRetriever.retrieve(url).getOrNull() ?: return@withContext false

            val pub = publicationOpener.open(asset, allowUserInteraction = false).getOrNull()
            publication = pub
            
            return@withContext publication != null
        } catch (e: Exception) {
            android.util.Log.e("EpubReaderEngine", "Failed to open EPUB", e)
            return@withContext false
        }
    }

    suspend fun getChapterContent(index: Int): String? = withContext(Dispatchers.IO) {
        val pub = publication ?: return@withContext null
        val links = pub.readingOrder
        if (index < 0 || index >= links.size) return@withContext null

        val link = links[index]
        val resource = pub.get(link)
        
        val bytes = resource?.read()?.getOrNull()
        if (bytes != null) String(bytes, Charsets.UTF_8) else null
    }

    fun getLocatorForChapter(index: Int): String? {
        val pub = publication ?: return null
        val links = pub.readingOrder
        if (index < 0 || index >= links.size) return null
        val link = links[index]
        val urlStr = link.href.toString()
        val locatorUrl = org.readium.r2.shared.util.Url(urlStr) ?: return null
        val locator = Locator(
            href = locatorUrl,
            mediaType = link.mediaType ?: org.readium.r2.shared.util.mediatype.MediaType.HTML,
            title = link.title,
            locations = Locator.Locations(progression = 0.0)
        )
        return locator.toJSON().toString()
    }

    fun getChapterIndexFromLocator(json: String): Int {
        val pub = publication ?: return 0
        try {
            val locator = Locator.fromJSON(JSONObject(json))
            if (locator != null) {
                val index = pub.readingOrder.indexOfFirst { it.href.toString() == locator.href.toString() }
                if (index != -1) return index
            }
        } catch (e: Exception) {
            android.util.Log.e("EpubReaderEngine", "Failed to parse locator", e)
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

    fun getTableOfContents(): List<EpubTocItem> {
        val pub = publication ?: return emptyList()
        val toc = mutableListOf<EpubTocItem>()
        
        // Try tableOfContents first
        if (pub.tableOfContents.isNotEmpty()) {
            for (link in pub.tableOfContents) {
                // Find chapter index by matching href in readingOrder
                val index = pub.readingOrder.indexOfFirst { it.href.toString() == link.href.toString() || link.href.toString().startsWith(it.href.toString() + "#") }
                if (index != -1) {
                    toc.add(EpubTocItem(title = link.title ?: "Chapter ${index + 1}", chapterIndex = index))
                }
            }
        }
        
        // Fallback to readingOrder if TOC is empty
        if (toc.isEmpty()) {
            pub.readingOrder.forEachIndexed { index, link ->
                toc.add(EpubTocItem(title = link.title ?: "Chapter ${index + 1}", chapterIndex = index))
            }
        }
        
        return toc.distinctBy { it.chapterIndex }
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
