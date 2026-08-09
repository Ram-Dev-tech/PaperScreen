package com.paperscreen.android.reader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperscreen.android.reader.settings.ReaderSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onSettingsChanged: (ReaderSettings) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Typography", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Font Size: ${settings.fontSize}sp")
            Slider(
                value = settings.fontSize.toFloat(),
                onValueChange = { onSettingsChanged(settings.copy(fontSize = it.toInt())) },
                valueRange = 12f..32f
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("SansSerif", "Serif", "Monospace").forEach { family ->
                    FilterChip(
                        selected = settings.fontFamily == family,
                        onClick = { onSettingsChanged(settings.copy(fontFamily = family)) },
                        label = { Text(family) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Normal", "Medium", "Bold").forEach { weight ->
                    FilterChip(
                        selected = settings.fontWeight == weight,
                        onClick = { onSettingsChanged(settings.copy(fontWeight = weight)) },
                        label = { Text(weight) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Spacing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Line Spacing: ${String.format("%.1f", settings.lineSpacing)}")
            Slider(
                value = settings.lineSpacing,
                onValueChange = { onSettingsChanged(settings.copy(lineSpacing = it)) },
                valueRange = 1.0f..2.0f
            )

            Text("Letter Spacing: ${String.format("%.1f", settings.letterSpacing)}")
            Slider(
                value = settings.letterSpacing,
                onValueChange = { onSettingsChanged(settings.copy(letterSpacing = it)) },
                valueRange = 0.0f..0.2f
            )

            Text("Paragraph Spacing: ${settings.paragraphSpacing.toInt()}dp")
            Slider(
                value = settings.paragraphSpacing,
                onValueChange = { onSettingsChanged(settings.copy(paragraphSpacing = it)) },
                valueRange = 0f..32f
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Layout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Narrow", "Normal", "Wide").forEach { margin ->
                    FilterChip(
                        selected = settings.margins == margin,
                        onClick = { onSettingsChanged(settings.copy(margins = margin)) },
                        label = { Text(margin) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Full", "Comfortable", "Narrow").forEach { width ->
                    FilterChip(
                        selected = settings.textWidth == width,
                        onClick = { onSettingsChanged(settings.copy(textWidth = width)) },
                        label = { Text(width) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Left", "Center", "Right", "Justify").forEach { align ->
                    FilterChip(
                        selected = settings.alignment == align,
                        onClick = { onSettingsChanged(settings.copy(alignment = align)) },
                        label = { Text(align) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onReset() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset to Default")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
