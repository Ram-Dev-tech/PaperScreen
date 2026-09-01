package com.paperscreen.android.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LauncherRepository(private val context: Context) {
    suspend fun getInstalledApps(): List<LauncherItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        val paperApps = listOf(
            PaperApp("Settings", PaperDestination.SETTINGS, "paper_settings")
        )

        val externalApps = apps.mapNotNull { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val name = resolveInfo.loadLabel(pm).toString()
            if (packageName == context.packageName) null // Skip ourselves (handled by internal PaperApps)
            else ExternalApp(name, packageName, packageName)
        }

        (paperApps + externalApps).sortedBy { it.label.lowercase() }
    }
}
