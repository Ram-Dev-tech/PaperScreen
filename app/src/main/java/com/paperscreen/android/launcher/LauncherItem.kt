package com.paperscreen.android.launcher

sealed interface LauncherItem {
    val label: String
    val iconKey: String
}

data class ExternalApp(
    val packageName: String,
    override val label: String
) : LauncherItem {
    override val iconKey: String get() = packageName
}

enum class PaperDestination {
    LIBRARY, SETTINGS
}

data class PaperApp(
    val destination: PaperDestination,
    override val label: String
) : LauncherItem {
    override val iconKey: String get() = "paper_${destination.name}"
}
