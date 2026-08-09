package com.paperscreen.android.dictionary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperscreen.android.dictionary.model.DictionaryDefinition
import com.paperscreen.android.dictionary.model.DictionaryLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryPopup(
    definitions: List<DictionaryDefinition>,
    availableLanguages: List<DictionaryLanguage>,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dictionary", style = MaterialTheme.typography.titleLarge)
                
                var expanded by remember { mutableStateOf(false) }
                val currentLangName = availableLanguages.find { it.code == currentLanguageCode }?.displayName ?: currentLanguageCode
                
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(currentLangName)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableLanguages.filter { it.isInstalled }.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    onLanguageSelected(lang.code)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (definitions.isEmpty()) {
                Text(
                    text = "No definition found for this word in the selected dictionary.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn {
                    items(definitions) { def ->
                        Column(modifier = Modifier.padding(bottom = 24.dp)) {
                            Text(text = def.word, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                def.partOfSpeech?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                def.pronunciation?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Text(
                                text = def.definition,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            def.example?.let {
                                Text(
                                    text = "\"$it\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
