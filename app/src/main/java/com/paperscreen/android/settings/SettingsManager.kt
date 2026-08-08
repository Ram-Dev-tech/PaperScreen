package com.paperscreen.android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "paperscreen_settings")

class SettingsManager(private val context: Context) {
    
    companion object {
        val IS_PAPER_MODE_ENABLED = booleanPreferencesKey("is_paper_mode_enabled")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery")
        val SHOW_SEARCH = booleanPreferencesKey("show_search")
    }
    
    val isPaperModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_PAPER_MODE_ENABLED] ?: true
    }
    
    suspend fun setPaperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PAPER_MODE_ENABLED] = enabled
        }
    }
    
    // Additional settings flows...
    val showClock: Flow<Boolean> = context.dataStore.data.map { it[SHOW_CLOCK] ?: true }
    val showDate: Flow<Boolean> = context.dataStore.data.map { it[SHOW_DATE] ?: true }
    val showBattery: Flow<Boolean> = context.dataStore.data.map { it[SHOW_BATTERY] ?: true }
    val showSearch: Flow<Boolean> = context.dataStore.data.map { it[SHOW_SEARCH] ?: true }
}
