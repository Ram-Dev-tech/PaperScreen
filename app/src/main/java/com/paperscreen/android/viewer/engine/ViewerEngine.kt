package com.paperscreen.android.viewer.engine

import android.content.Context
import android.net.Uri

interface ViewerEngine {
    suspend fun load(context: Context, uri: Uri): Boolean
    fun close()
}
