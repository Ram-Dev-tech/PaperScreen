package com.paperscreen.android.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Process
import android.os.UserHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class LauncherRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val packageManager = context.packageManager

    private val paperApps = listOf(
        PaperApp(PaperDestination.LIBRARY, "Paper Reader"),
        PaperApp(PaperDestination.SETTINGS, "Settings")
    )

    fun getLaunchableApps(): Flow<List<LauncherItem>> = callbackFlow {
        val userHandle = Process.myUserHandle()
        
        fun loadApps() {
            val activityList = launcherApps.getActivityList(null, userHandle)
            val externalApps = activityList.map { activityInfo ->
                ExternalApp(
                    packageName = activityInfo.componentName.packageName,
                    label = activityInfo.label?.toString() ?: activityInfo.componentName.packageName
                )
            }.distinctBy { it.packageName }
            
            val combined = processLauncherItems(externalApps, paperApps, context.packageName)
            trySend(combined)
        }

        // Initial load
        loadApps()

        // Register callback for changes
        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                if (user == userHandle) loadApps()
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                if (user == userHandle) loadApps()
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                if (user == userHandle) loadApps()
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                if (user == userHandle) loadApps()
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                if (user == userHandle) loadApps()
            }
        }

        launcherApps.registerCallback(callback)

        awaitClose {
            launcherApps.unregisterCallback(callback)
        }
    }.flowOn(Dispatchers.IO)

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LauncherRepository", "Failed to launch app", e)
            false
        }
    }

    companion object {
        fun processLauncherItems(
            externalApps: List<ExternalApp>,
            paperApps: List<PaperApp>,
            selfPackageName: String
        ): List<LauncherItem> {
            val uniqueExternal = externalApps
                .filter { it.packageName != selfPackageName }
                .distinctBy { it.packageName }
            
            return (uniqueExternal + paperApps).sortedBy { it.label.lowercase() }
        }
    }
}
