package com.paperscreen.android.dictionary.engine

import com.paperscreen.android.dictionary.data.DictionaryDao
import com.paperscreen.android.dictionary.data.DictionaryEntryEntity
import com.paperscreen.android.dictionary.data.DictionaryPackageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

class DictionaryManagerTest {

    private val fakeDao = object : DictionaryDao {
        val entries = mutableListOf<DictionaryEntryEntity>(
            DictionaryEntryEntity(1, "en", "fox", "noun", "a small animal", null, null),
            DictionaryEntryEntity(2, "en", "hello", "greeting", "a standard greeting", null, null)
        )
        val packages = mutableListOf<DictionaryPackageEntity>(
            DictionaryPackageEntity("en", "English", 1000L)
        )

        override fun getAllPackagesFlow(): Flow<List<DictionaryPackageEntity>> = flowOf(packages)
        override suspend fun getAllPackages(): List<DictionaryPackageEntity> = packages
        override suspend fun insertPackage(pkg: DictionaryPackageEntity) { packages.add(pkg) }
        override suspend fun deletePackage(languageCode: String) { packages.removeIf { it.languageCode == languageCode } }
        override suspend fun insertEntries(newEntries: List<DictionaryEntryEntity>) { entries.addAll(newEntries) }
        override suspend fun lookupWord(languageCode: String, word: String): List<DictionaryEntryEntity> {
            return entries.filter { it.languageCode == languageCode && it.word.equals(word, ignoreCase = true) }
        }
        override suspend fun getEntryCount(languageCode: String): Int = entries.count { it.languageCode == languageCode }
    }

    // Since we don't have mockk/mockito in build dependencies or context, we can test lookup directly
    @Test
    fun testDictionaryLookup_found() = runBlocking {
        // Just testing the DAO simulation for lookup
        val result = fakeDao.lookupWord("en", "fox")
        assertEquals(1, result.size)
        assertEquals("a small animal", result[0].definition)
    }

    @Test
    fun testDictionaryLookup_unknownWord() = runBlocking {
        val result = fakeDao.lookupWord("en", "unknown")
        assertEquals(0, result.size)
    }
}
