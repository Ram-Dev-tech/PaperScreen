package com.paperscreen.android.papermode

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AppRule(
    val packageName: String,
    val usePaperView: Boolean = true,
    val showInLauncher: Boolean = true,
    val allowNotifications: Boolean = false,
    val dailyLimitMinutes: Int? = null
)

class AppRulesRepository(private val context: Context) {

    companion object {
        val APP_RULES_KEY = stringPreferencesKey("app_rules_json")
    }

    val appRules: Flow<List<AppRule>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[APP_RULES_KEY]
            if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    Json.decodeFromString<List<AppRule>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun saveRule(rule: AppRule) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[APP_RULES_KEY]
            val currentRules = if (jsonString.isNullOrEmpty()) {
                emptyList()
            } else {
                try {
                    Json.decodeFromString<List<AppRule>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val updatedRules = currentRules.toMutableList()
            val existingIndex = updatedRules.indexOfFirst { it.packageName == rule.packageName }
            if (existingIndex != -1) {
                updatedRules[existingIndex] = rule
            } else {
                updatedRules.add(rule)
            }

            preferences[APP_RULES_KEY] = Json.encodeToString(updatedRules)
        }
    }

    fun getRuleForPackage(packageName: String): Flow<AppRule?> {
        return appRules.map { rules ->
            rules.find { it.packageName == packageName }
        }
    }
}
