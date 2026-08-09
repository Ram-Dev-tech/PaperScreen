package com.paperscreen.android.dictionary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

@Composable
fun WordActionMenu(
    anchorX: Int,
    anchorY: Int,
    onDictionary: () -> Unit,
    onHighlight: () -> Unit,
    onMark: () -> Unit,
    onSearchGoogle: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Popup(
        offset = IntOffset(anchorX, anchorY),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
        ) {
            ActionMenuItem(text = "Dictionary", onClick = onDictionary)
            ActionMenuItem(text = "Highlight", onClick = onHighlight)
            ActionMenuItem(text = "Mark", onClick = onMark)
            ActionMenuItem(text = "Search Google", onClick = onSearchGoogle)
            ActionMenuItem(text = "Copy", onClick = onCopy)
            ActionMenuItem(text = "Share", onClick = onShare)
        }
    }
}

@Composable
private fun ActionMenuItem(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
