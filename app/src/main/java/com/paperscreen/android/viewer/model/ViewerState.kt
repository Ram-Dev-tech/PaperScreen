package com.paperscreen.android.viewer.model

import android.net.Uri
import com.paperscreen.android.viewer.engine.PdfViewerEngine
import com.paperscreen.android.viewer.engine.TxtViewerEngine

sealed class ViewerState {
    object Loading : ViewerState()
    
    data class Pdf(val engine: PdfViewerEngine) : ViewerState()
    data class Txt(val engine: TxtViewerEngine) : ViewerState()
    data class Image(val uri: Uri) : ViewerState()
    
    data class Error(val message: String) : ViewerState()
    object Unsupported : ViewerState()
}
