package com.paperscreen.android.launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.launcher.ExternalApp
import com.paperscreen.android.launcher.IconEngine
import com.paperscreen.android.launcher.LauncherItem
import com.paperscreen.android.launcher.PaperApp
import com.paperscreen.android.launcher.PaperDestination

@Composable
fun AppLauncherScreen(
    viewModel: AppLauncherViewModel = viewModel(),
    onNavigateToPaperApp: (PaperDestination) -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { 
                Text(
                    "Search Apps...", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant 
                ) 
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Grid displaying apps
        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps, key = { it.iconKey }) { app ->
                AppLauncherItem(
                    item = app,
                    iconEngine = viewModel.iconEngine,
                    onClick = {
                        when (app) {
                            is ExternalApp -> viewModel.launchExternalApp(app.packageName)
                            is PaperApp -> onNavigateToPaperApp(app.destination)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppLauncherItem(
    item: LauncherItem,
    iconEngine: IconEngine,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface) // optional, just to have a background if transparent
        ) {
            when (item) {
                is ExternalApp -> {
                    val iconBitmap = iconEngine.getExternalAppIcon(item.packageName)
                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap.asImageBitmap(),
                            contentDescription = item.label,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = item.label.firstOrNull()?.toString() ?: "?",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is PaperApp -> {
                    val iconVector = when(item.destination) {
                        PaperDestination.LIBRARY -> Icons.Default.MenuBook
                        PaperDestination.SETTINGS -> Icons.Default.Settings
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = item.label,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App name
        Text(
            text = item.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            lineHeight = 14.sp
        )
    }
}
