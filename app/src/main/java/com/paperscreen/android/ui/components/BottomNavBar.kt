package com.paperscreen.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lens
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavBar(selectedItem: String = "Focus") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarIcon(
            icon = Icons.Outlined.Home,
            contentDescription = "Home",
            isSelected = selectedItem == "Home"
        )
        NavBarIcon(
            icon = Icons.Outlined.Apps,
            contentDescription = "Apps",
            isSelected = selectedItem == "Apps"
        )
        NavBarIcon(
            icon = Icons.Outlined.Lens, // Placeholder for Focus
            contentDescription = "Focus",
            isSelected = selectedItem == "Focus"
        )
        NavBarIcon(
            icon = Icons.Outlined.Search,
            contentDescription = "Search",
            isSelected = selectedItem == "Search"
        )
        NavBarIcon(
            icon = Icons.Outlined.Settings,
            contentDescription = "Settings",
            isSelected = selectedItem == "Settings"
        )
    }
}

@Composable
private fun NavBarIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    isSelected: Boolean
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { }
            .background(
                if (isSelected) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.background
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
