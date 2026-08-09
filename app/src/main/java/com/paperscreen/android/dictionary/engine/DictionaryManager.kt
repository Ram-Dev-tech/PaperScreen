package com.paperscreen.android.dictionary.engine

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paperscreen.android.dictionary.data.DictionaryDatabase
import com.paperscreen.android.dictionary.data.DictionaryEntryEntity
import com.paperscreen.android.dictionary.data.DictionaryPackageEntity
import com.paperscreen.android.dictionary.model.DictionaryDefinition
import com.paperscreen.android.dictionary.model.DictionaryLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

val Context.dictionaryDataStore by preferencesDataStore(name = "dictionary_settings")

class DictionaryManager(
    private val context: Context,
    private val dao: com.paperscreen.android.dictionary.data.DictionaryDao = DictionaryDatabase.getDatabase(context).dictionaryDao()
) {
    
    private val currentLanguageKey = stringPreferencesKey("current_dictionary_language")

    // The available languages to download (hardcoded list per phase requirements)
    private val availableLanguages = listOf(
        DictionaryLanguage("en", "English", isDefault = true),
        DictionaryLanguage("hi", "Hindi"),
        DictionaryLanguage("es", "Spanish"),
        DictionaryLanguage("fr", "French"),
        DictionaryLanguage("de", "German"),
        DictionaryLanguage("it", "Italian"),
        DictionaryLanguage("pt", "Portuguese"),
        DictionaryLanguage("ru", "Russian"),
        DictionaryLanguage("ja", "Japanese"),
        DictionaryLanguage("zh", "Chinese")
    )

    fun getAvailableLanguages(): Flow<List<DictionaryLanguage>> {
        val assetList = try {
            context.assets.list("dictionaries")?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return dao.getAllPackagesFlow().map { installedPkgs ->
            val installedCodes = installedPkgs.map { it.languageCode }.toSet()
            availableLanguages.map { lang ->
                val hasAsset = assetList.contains("${lang.code}.json")
                lang.copy(
                    isInstalled = installedCodes.contains(lang.code) || lang.isDefault,
                    isBundled = hasAsset
                )
            }
        }
    }

    suspend fun getCurrentLanguage(): String {
        return context.dictionaryDataStore.data.map { prefs ->
            prefs[currentLanguageKey] ?: "en"
        }.first()
    }

    suspend fun setCurrentLanguage(languageCode: String) {
        context.dictionaryDataStore.edit { prefs ->
            prefs[currentLanguageKey] = languageCode
        }
    }

    suspend fun ensureDefaultDictionaryInstalled() {
        if (dao.getEntryCount("en") == 0) {
            installDictionary("en")
        }
    }

    suspend fun installDictionary(languageCode: String): Boolean {
        try {
            val fileName = "dictionaries/$languageCode.json"
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            
            val entries = mutableListOf<DictionaryEntryEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                entries.add(
                    DictionaryEntryEntity(
                        languageCode = languageCode,
                        word = obj.getString("word"),
                        partOfSpeech = obj.optString("partOfSpeech", null).takeIf { it.isNotBlank() },
                        definition = obj.getString("definition"),
                        pronunciation = obj.optString("pronunciation", null).takeIf { it.isNotBlank() },
                        example = obj.optString("example", null).takeIf { it.isNotBlank() }
                    )
                )
            }

            val langDef = availableLanguages.find { it.code == languageCode }
            val pkgName = langDef?.displayName ?: languageCode

            dao.insertPackage(DictionaryPackageEntity(languageCode, pkgName, System.currentTimeMillis()))
            dao.insertEntries(entries)
            return true
        } catch (e: Exception) {
            android.util.Log.e("DictionaryManager", "Failed to parse stardict", e)
            return false
        }
    }

    suspend fun deleteDictionary(languageCode: String) {
        if (languageCode == "en") return // Cannot delete default
        dao.deletePackage(languageCode)
        
        // Reset current language to english if we deleted the current one
        if (getCurrentLanguage() == languageCode) {
            setCurrentLanguage("en")
        }
    }

    suspend fun lookup(rawWord: String, languageCode: String): List<DictionaryDefinition> {
        val word = WordNormalizer.normalize(rawWord)
        if (word.isEmpty()) return emptyList()

        val entries = dao.lookupWord(languageCode, word)
        return entries.map {
            DictionaryDefinition(
                word = it.word,
                partOfSpeech = it.partOfSpeech,
                definition = it.definition,
                pronunciation = it.pronunciation,
                example = it.example
            )
        }
    }
}
