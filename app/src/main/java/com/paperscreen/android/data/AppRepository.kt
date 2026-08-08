package com.paperscreen.android.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class AppInfo(
    val packageName: String,
    val label: String
)

class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val packageManager = context.packageManager

    fun getLaunchableApps(): Flow<List<AppInfo>> = flow {
        val userHandle = Process.myUserHandle()
        val activityList = launcherApps.getActivityList(null, userHandle)
        
        val apps = activityList.map { activityInfo ->
            AppInfo(
                packageName = activityInfo.componentName.packageName,
                label = activityInfo.label.toString()
            )
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        
        emit(apps)
    }.flowOn(Dispatchers.IO)

    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
