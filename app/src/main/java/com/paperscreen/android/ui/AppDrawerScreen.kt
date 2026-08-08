package com.paperscreen.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.data.AppInfo
import com.paperscreen.android.icon.IconEngine

@Composable
fun AppDrawerScreen(
    viewModel: AppDrawerViewModel = viewModel()
) {
    val apps by viewModel.apps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

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
        
        // 4-column grid displaying apps
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppDrawerItem(
                    appInfo = app,
                    iconEngine = viewModel.iconEngine,
                    isDarkTheme = isDarkTheme,
                    onClick = { viewModel.launchApp(app.packageName) }
                )
            }
        }
    }
}

@Composable
fun AppDrawerItem(
    appInfo: AppInfo,
    iconEngine: IconEngine,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val iconBitmap = iconEngine.getPaperIcon(appInfo.packageName, isDarkTheme)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap.asImageBitmap(),
                contentDescription = appInfo.label,
                modifier = Modifier.size(48.dp)
            )
        } else {
            // Icon placeholder if fail to load
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // App name
        Text(
            text = appInfo.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
