package com.paperscreen.android.reader.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.withTimeoutOrNull

suspend fun PointerInputScope.detectCustomLongPress(
    durationMillis: Long = 1500L,
    onLongPress: (Offset) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val touchSlop = viewConfiguration.touchSlop
        val upOrCancel = withTimeoutOrNull(durationMillis) {
            var event: PointerEvent
            var isMoved = false
            do {
                event = awaitPointerEvent(PointerEventPass.Main)
                val pos = event.changes.firstOrNull()?.position
                if (pos != null && (pos - down.position).getDistance() > touchSlop) {
                    isMoved = true
                }
            } while (event.changes.any { it.pressed } && !isMoved)
            true // returned if released or moved before timeout
        }
        
        if (upOrCancel == null) {
            // Timeout happened before movement or release! Long press!
            onLongPress(down.position)
        }
    }
}
