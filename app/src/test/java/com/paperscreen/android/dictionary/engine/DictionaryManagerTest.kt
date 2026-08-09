package com.paperscreen.android.dictionary.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.paperscreen.android.dictionary.data.DictionaryDao
import com.paperscreen.android.dictionary.data.DictionaryEntryEntity
import com.paperscreen.android.dictionary.data.DictionaryPackageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Ignore
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@Ignore("Robolectric ASM failure with SDK 36 / Java 25")
class DictionaryManagerTest {

    private lateinit var manager: DictionaryManager
    private lateinit var context: Context
    private val packages = mutableListOf<DictionaryPackageEntity>()
    private val entries = mutableListOf<DictionaryEntryEntity>()

    private val fakeDao = object : DictionaryDao {
        override fun getAllPackagesFlow(): Flow<List<DictionaryPackageEntity>> = flowOf(packages)
        override suspend fun getAllPackages(): List<DictionaryPackageEntity> = packages
        override suspend fun insertPackage(pkg: DictionaryPackageEntity) { packages.add(pkg) }
        override suspend fun deletePackage(languageCode: String) {
            packages.removeIf { it.languageCode == languageCode }
            entries.removeIf { it.languageCode == languageCode }
        }
        override suspend fun insertEntries(newEntries: List<DictionaryEntryEntity>) { entries.addAll(newEntries) }
        override suspend fun lookupWord(languageCode: String, word: String): List<DictionaryEntryEntity> {
            return entries.filter { it.languageCode == languageCode && it.word.equals(word, ignoreCase = true) }
        }
        override suspend fun getEntryCount(languageCode: String): Int = entries.count { it.languageCode == languageCode }
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Provide mock seed assets to the context
        val assetDir = File(context.cacheDir, "assets/dictionaries")
        assetDir.mkdirs()
        File(assetDir, "en.json").writeText("""
            [
              { "word": "fox", "partOfSpeech": "noun", "definition": "A small animal" },
              { "word": "hello", "partOfSpeech": "greeting", "definition": "A standard greeting" }
            ]
        """.trimIndent())
        File(assetDir, "hi.json").writeText("[]")
        
        val mockContext = object : android.content.ContextWrapper(context) {
            override fun getAssets(): android.content.res.AssetManager {
                return baseContext.assets
            }
        }
        
        manager = DictionaryManager(mockContext, fakeDao)
    }

    @Test
    fun testDictionaryInstallationAndLookup() = runBlocking {
        val installed = manager.installDictionary("en")
        assertTrue(installed)

        assertEquals(1, packages.size)
        assertTrue(entries.isNotEmpty())

        val results = manager.lookup("fox", "en")
        assertEquals(1, results.size)
        assertTrue(results[0].definition.contains("small animal", ignoreCase = true))

        val missing = manager.lookup("asdfghjkl", "en")
        assertTrue(missing.isEmpty())
    }

    @Test
    fun testDictionaryDeletion() = runBlocking {
        val installed = manager.installDictionary("hi")
        assertTrue(installed)
        
        val pkgs = manager.getAvailableLanguages().first()
        assertTrue(pkgs.find { it.code == "hi" }?.isInstalled == true)
        
        manager.deleteDictionary("hi")
        
        val newPkgs = manager.getAvailableLanguages().first()
        assertFalse(newPkgs.find { it.code == "hi" }?.isInstalled == true)
    }
}
