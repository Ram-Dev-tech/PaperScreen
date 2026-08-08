package com.paperscreen.android.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paperscreen.android.papermode.DitheringMode
import com.paperscreen.android.papermode.PaperModeSettingsManager
import com.paperscreen.android.papermode.PaperModeType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperModeSettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsManager = remember { PaperModeSettingsManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val scrollState = rememberScrollState()

    val isMasterEnabled by settingsManager.masterPaperModeEnabled.collectAsState(initial = true)
    val paperModeType by settingsManager.paperModeType.collectAsState(initial = PaperModeType.PAPER)
    
    val contrast by settingsManager.contrast.collectAsState(initial = 60)
    val brightness by settingsManager.brightness.collectAsState(initial = 70)
    val strength by settingsManager.strength.collectAsState(initial = 80)
    val threshold by settingsManager.threshold.collectAsState(initial = 50)
    val dithering by settingsManager.dithering.collectAsState(initial = DitheringMode.MEDIUM)
    
    var expanded by remember { mutableStateOf(false) }

    val ditheringOptions = listOf(DitheringMode.LOW, DitheringMode.MEDIUM, DitheringMode.HIGH)
    val ditheringLabels = mapOf(
        DitheringMode.LOW to "Low",
        DitheringMode.MEDIUM to "Medium",
        DitheringMode.HIGH to "High"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, start = 24.dp, end = 24.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "PAPER MODE SETTINGS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Master Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Master Paper Mode",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isMasterEnabled,
                onCheckedChange = { 
                    coroutineScope.launch { settingsManager.setMasterPaperModeEnabled(it) } 
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onBackground,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Options
        Text(
            text = "Mode",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val modes = listOf(
            PaperModeType.PAPER to "Paper (Recommended)",
            PaperModeType.GRAYSCALE to "Grayscale",
            PaperModeType.ORIGINAL to "Original"
        )
        
        modes.forEach { (modeType, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        coroutineScope.launch { settingsManager.setPaperModeType(modeType) }
                    }
                    .padding(vertical = 12.dp)
            ) {
                RadioButton(
                    selected = (modeType == paperModeType),
                    onClick = { 
                        coroutineScope.launch { settingsManager.setPaperModeType(modeType) }
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onBackground,
                        unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sliders
        SettingsSlider("Contrast", contrast / 100f) { 
            coroutineScope.launch { settingsManager.setContrast((it * 100).toInt()) }
        }
        SettingsSlider("Brightness", brightness / 100f) { 
            coroutineScope.launch { settingsManager.setBrightness((it * 100).toInt()) }
        }
        SettingsSlider("Strength", strength / 100f) { 
            coroutineScope.launch { settingsManager.setStrength((it * 100).toInt()) }
        }
        SettingsSlider("Luminance Threshold", threshold / 100f) { 
            coroutineScope.launch { settingsManager.setThreshold((it * 100).toInt()) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dithering Dropdown
        Text(
            text = "Dithering",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = ditheringLabels[dithering] ?: "Medium",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                ditheringOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = ditheringLabels[option] ?: "",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            coroutineScope.launch { settingsManager.setDithering(option) }
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        )
    }
}
