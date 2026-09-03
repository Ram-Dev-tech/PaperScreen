package com.paperscreen.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerMode(val durationSeconds: Long) {
    POMODORO(25 * 60),
    STANDARD(5 * 60)
}

class TimerViewModel : ViewModel() {
    private val _timerMode = MutableStateFlow(TimerMode.POMODORO)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _timeRemaining = MutableStateFlow(TimerMode.POMODORO.durationSeconds)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
            }
            _isRunning.value = false
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()
        _timeRemaining.value = _timerMode.value.durationSeconds
    }

    fun switchMode(mode: TimerMode) {
        if (_timerMode.value == mode) return
        pauseTimer()
        _timerMode.value = mode
        _timeRemaining.value = mode.durationSeconds
    }
}
