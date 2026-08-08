package com.paperscreen.android.focus

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusStatsManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("focus_stats_prefs", Context.MODE_PRIVATE)

    // Dummy flows for focus score, screen time, unlocks, longest session
    private val _focusScore = MutableStateFlow(85)
    val focusScore: StateFlow<Int> = _focusScore.asStateFlow()

    private val _screenTimeMinutes = MutableStateFlow(0)
    val screenTimeMinutes: StateFlow<Int> = _screenTimeMinutes.asStateFlow()

    private val _unlocks = MutableStateFlow(prefs.getInt("unlocks", 0))
    val unlocks: StateFlow<Int> = _unlocks.asStateFlow()

    private val _longestSessionMinutes = MutableStateFlow(prefs.getInt("longest_session", 0))
    val longestSessionMinutes: StateFlow<Int> = _longestSessionMinutes.asStateFlow()

    private var activeSessionJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Resume tracking if a session was left active
        if (prefs.getBoolean("is_session_active", false)) {
            startTrackingSession()
        } else {
            // Provide some mock baseline if there's no active session
            _screenTimeMinutes.value = 125 // 2h 5m dummy value
        }
    }

    fun startFocusSession() {
        prefs.edit()
            .putBoolean("is_session_active", true)
            .putLong("session_start_time", System.currentTimeMillis())
            .apply()
            
        startTrackingSession()
    }

    fun endFocusSession() {
        prefs.edit().putBoolean("is_session_active", false).apply()
        activeSessionJob?.cancel()
        activeSessionJob = null
        
        val currentSessionMinutes = _screenTimeMinutes.value
        val longest = _longestSessionMinutes.value
        if (currentSessionMinutes > longest) {
            _longestSessionMinutes.value = currentSessionMinutes
            prefs.edit().putInt("longest_session", currentSessionMinutes).apply()
        }
        
        // Reset or keep screen time dummy logic as appropriate
    }

    fun recordUnlock() {
        val currentUnlocks = _unlocks.value + 1
        _unlocks.value = currentUnlocks
        prefs.edit().putInt("unlocks", currentUnlocks).apply()
    }

    private fun startTrackingSession() {
        activeSessionJob?.cancel()
        activeSessionJob = scope.launch {
            while (true) {
                val startTime = prefs.getLong("session_start_time", System.currentTimeMillis())
                val elapsedMinutes = ((System.currentTimeMillis() - startTime) / (1000 * 60)).toInt()
                _screenTimeMinutes.value = elapsedMinutes
                delay(60000) // Update every minute
            }
        }
    }
}
