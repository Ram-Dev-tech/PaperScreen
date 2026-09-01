package com.paperscreen.android.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paperscreen.android.launcher.ExternalApp
import com.paperscreen.android.launcher.IconEngine
import com.paperscreen.android.launcher.LauncherItem
import com.paperscreen.android.launcher.LauncherRepository
import com.paperscreen.android.launcher.PaperApp
import com.paperscreen.android.launcher.PaperDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppLauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)
    val iconEngine = IconEngine(application)

    private val _apps = MutableStateFlow<List<LauncherItem>>(emptyList())
    val apps: StateFlow<List<LauncherItem>> = _apps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allApps: List<LauncherItem> = emptyList()

    init {
        viewModelScope.launch {
            val appList = repository.getInstalledApps()
            allApps = appList
            filterApps(_searchQuery.value)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterApps(query)
    }

    private fun filterApps(query: String) {
        _apps.value = filterLauncherItems(allApps, query)
    }

    fun launchExternalApp(packageName: String) {
        val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(it)
        }
    }

    companion object {
        fun filterLauncherItems(items: List<LauncherItem>, query: String): List<LauncherItem> {
            if (query.isBlank()) {
                return items
            }
            return items.filter {
                it.label.contains(query, ignoreCase = true)
            }
        }
    }
}
