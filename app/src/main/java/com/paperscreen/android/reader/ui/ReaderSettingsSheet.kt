package com.paperscreen.android.reader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    lineSpacing: Float,
    onLineSpacingChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Typography", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Font Size", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = fontSize,
                onValueChange = onFontSizeChange,
                valueRange = 12f..36f
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Line Spacing", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = lineSpacing,
                onValueChange = onLineSpacingChange,
                valueRange = 1.0f..2.5f
            )
            
            Spacer(modifier = Modifier.height(48.dp)) // Padding for bottom nav bar area
        }
    }
}
