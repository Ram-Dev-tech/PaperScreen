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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SettingsItemData(val title: String, val icon: ImageVector)

@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit = {}
) {
    val settingsItems = listOf(
        SettingsItemData("Appearance", Icons.Outlined.Palette),
        SettingsItemData("Paper Mode", Icons.Outlined.Article),
        SettingsItemData("Focus", Icons.Outlined.CenterFocusWeak),
        SettingsItemData("Apps & Rules", Icons.Outlined.Apps),
        SettingsItemData("Schedule", Icons.Outlined.Schedule),
        SettingsItemData("Gestures", Icons.Outlined.TouchApp),
        SettingsItemData("Control Center", Icons.Outlined.Tune),
        SettingsItemData("Backup & Restore", Icons.Outlined.Backup),
        SettingsItemData("About", Icons.Outlined.Info)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LazyColumn {
            items(settingsItems) { item ->
                SettingsRow(
                    item = item,
                    onClick = { onNavigate(item.title) }
                )
            }
        }
    }
}

@Composable
fun SettingsRow(item: SettingsItemData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
