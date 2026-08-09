package com.paperscreen.android.dictionary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary_packages ORDER BY displayName ASC")
    fun getAllPackagesFlow(): Flow<List<DictionaryPackageEntity>>

    @Query("SELECT * FROM dictionary_packages")
    suspend fun getAllPackages(): List<DictionaryPackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: DictionaryPackageEntity)

    @Query("DELETE FROM dictionary_packages WHERE languageCode = :languageCode")
    suspend fun deletePackage(languageCode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DictionaryEntryEntity>)

    @Query("SELECT * FROM dictionary_entries WHERE languageCode = :languageCode AND word = :word COLLATE NOCASE")
    suspend fun lookupWord(languageCode: String, word: String): List<DictionaryEntryEntity>

    @Query("SELECT COUNT(*) FROM dictionary_entries WHERE languageCode = :languageCode")
    suspend fun getEntryCount(languageCode: String): Int
}
