package com.paperscreen.android.reader.settings

data class ReaderSettings(
    val fontSize: Int = 18,
    val fontFamily: String = "SansSerif",
    val fontWeight: String = "Normal",
    val lineSpacing: Float = 1.4f,
    val letterSpacing: Float = 0.0f,
    val paragraphSpacing: Float = 16.0f,
    val margins: String = "Normal",
    val textWidth: String = "Full",
    val alignment: String = "Left"
)
