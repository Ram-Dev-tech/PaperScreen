package com.paperscreen.android.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.launcher.ExternalApp
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
        
        // List displaying apps (Text only)
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps, key = { it.iconKey }) { app ->
                AppLauncherItemTextOnly(
                    item = app,
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
fun AppLauncherItemTextOnly(
    item: LauncherItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = item.label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
