package com.paperscreen.android.dictionary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paperscreen.android.dictionary.engine.DictionaryManager
import com.paperscreen.android.dictionary.model.DictionaryLanguage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryDownloadScreen(
    onBack: () -> Unit,
    dictionaryManager: DictionaryManager = DictionaryManager(LocalContext.current)
) {
    val languages by dictionaryManager.getAvailableLanguages().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    
    // Ensure English is installed
    LaunchedEffect(Unit) {
        dictionaryManager.ensureDefaultDictionaryInstalled()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dictionaries (Seed Data)") },
                navigationIcon = {
                    Text(
                        text = "Back",
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Note: These are small open-license seed dictionaries for testing offline architecture.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn {
                items(languages) { lang ->
                    LanguageItem(
                        language = lang,
                        isLoading = isLoading,
                        onInstall = {
                            coroutineScope.launch {
                                isLoading = true
                                dictionaryManager.installDictionary(lang.code)
                                isLoading = false
                            }
                        },
                        onDelete = {
                            coroutineScope.launch {
                                isLoading = true
                                dictionaryManager.deleteDictionary(lang.code)
                                isLoading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: DictionaryLanguage,
    isLoading: Boolean,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = language.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (language.isInstalled) "Installed" else "Not installed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (language.isDefault) {
                Text(
                    text = "Default",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (language.isInstalled) {
            if (!language.isDefault) {
                TextButton(onClick = onDelete, enabled = !isLoading) {
                    Text("Delete")
                }
            }
        } else {
            if (language.isBundled) {
                TextButton(onClick = onInstall, enabled = !isLoading) {
                    Text("Install")
                }
            } else {
                Text(
                    text = "No dataset",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}
