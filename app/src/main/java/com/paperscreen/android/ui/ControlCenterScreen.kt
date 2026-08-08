package com.paperscreen.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ControlCenterScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Top Bar: Time/Date & Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "10:30",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Thu, Aug 8",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Quick Settings Grid (4 rows, 2 columns)
            val quickSettings = listOf(
                Pair("Wi-Fi", Icons.Outlined.Wifi),
                Pair("Bluetooth", Icons.Outlined.Bluetooth),
                Pair("Mobile Data", Icons.Outlined.DataUsage),
                Pair("Hotspot", Icons.Outlined.WifiTethering),
                Pair("Auto Rotate", Icons.Outlined.ScreenRotation),
                Pair("Dark Mode", Icons.Outlined.DarkMode),
                Pair("DND", Icons.Outlined.DoNotDisturbOn),
                Pair("Battery Saver", Icons.Outlined.BatterySaver)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in quickSettings.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickSettingTile(
                            modifier = Modifier.weight(1f),
                            title = quickSettings[i].first,
                            icon = quickSettings[i].second,
                            isActive = i == 0 // Just making Wi-Fi active as dummy data
                        )
                        if (i + 1 < quickSettings.size) {
                            QuickSettingTile(
                                modifier = Modifier.weight(1f),
                                title = quickSettings[i + 1].first,
                                icon = quickSettings[i + 1].second,
                                isActive = false
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Brightness and Volume Sliders
            var brightness by remember { mutableFloatStateOf(0.7f) }
            var volume by remember { mutableFloatStateOf(0.4f) }
            
            ControlSlider(
                icon = Icons.Outlined.LightMode,
                value = brightness,
                onValueChange = { brightness = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            ControlSlider(
                icon = Icons.Outlined.VolumeUp,
                value = volume,
                onValueChange = { volume = it }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Media Playback Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = "Cover",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Continue Reading",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Kindle",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Outlined.SkipPrevious,
                                contentDescription = "Previous",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Play",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Outlined.SkipNext,
                                contentDescription = "Next",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun QuickSettingTile(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    isActive: Boolean
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background)
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun ControlSlider(
    icon: ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.onBackground,
                activeTrackColor = MaterialTheme.colorScheme.onBackground,
                inactiveTrackColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
