package com.paperscreen.android.reader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperscreen.android.reader.data.NoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSheet(
    notes: List<NoteEntity>,
    highlightedText: String,
    onAddNote: (String) -> Unit,
    onUpdateNote: (NoteEntity, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onDeleteHighlight: () -> Unit,
    onDismiss: () -> Unit
) {
    var newNoteText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text("Notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            
            Text(
                text = "\"$highlightedText\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(notes) { note ->
                    var isEditing by remember { mutableStateOf(false) }
                    var editText by remember { mutableStateOf(note.text) }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editText,
                                    onValueChange = { editText = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { isEditing = false }) { Text("Cancel") }
                                    TextButton(onClick = {
                                        onUpdateNote(note, editText)
                                        isEditing = false
                                    }) { Text("Save") }
                                }
                            } else {
                                Text(note.text, style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                    TextButton(onClick = { isEditing = true }) { Text("Edit") }
                                    TextButton(onClick = { onDeleteNote(note) }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newNoteText,
                onValueChange = { newNoteText = it },
                label = { Text("New note") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp)
            ) {
                TextButton(onClick = {
                    onDeleteHighlight()
                    onDismiss()
                }) {
                    Text("Remove Highlight", color = MaterialTheme.colorScheme.onSurface)
                }
                
                Button(
                    onClick = {
                        if (newNoteText.isNotBlank()) {
                            onAddNote(newNoteText)
                            newNoteText = ""
                        }
                    },
                    enabled = newNoteText.isNotBlank()
                ) {
                    Text("Add Note")
                }
            }
        }
    }
}
