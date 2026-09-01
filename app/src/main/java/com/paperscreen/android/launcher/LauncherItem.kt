package com.paperscreen.android.launcher

sealed class LauncherItem {
    abstract val label: String
    abstract val iconKey: String
}

data class ExternalApp(
    override val label: String,
    val packageName: String,
    override val iconKey: String
) : LauncherItem()

data class PaperApp(
    override val label: String,
    val destination: PaperDestination,
    override val iconKey: String
) : LauncherItem()

enum class PaperDestination {
    SETTINGS
}
