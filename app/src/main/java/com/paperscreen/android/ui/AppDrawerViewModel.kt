package com.paperscreen.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.data.AppInfo
import com.paperscreen.android.data.AppRepository
import com.paperscreen.android.icon.IconEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppDrawerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    val iconEngine = IconEngine(application)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allApps: List<AppInfo> = emptyList()

    init {
        viewModelScope.launch {
            repository.getLaunchableApps().collect { appList ->
                allApps = appList
                filterApps(_searchQuery.value)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterApps(query)
    }

    private fun filterApps(query: String) {
        if (query.isBlank()) {
            _apps.value = allApps
        } else {
            _apps.value = allApps.filter {
                it.label.contains(query, ignoreCase = true)
            }
        }
    }

    fun launchApp(packageName: String) {
        repository.launchApp(packageName)
    }
}
