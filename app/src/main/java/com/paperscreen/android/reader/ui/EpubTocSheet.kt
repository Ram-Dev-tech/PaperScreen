package com.paperscreen.android.reader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paperscreen.android.reader.parser.EpubTocItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubTocSheet(
    toc: List<EpubTocItem>,
    currentChapterIndex: Int,
    onChapterSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Contents",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(toc) { item ->
                    val isSelected = item.chapterIndex == currentChapterIndex
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChapterSelect(item.chapterIndex) }
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                }
            }
        }
    }
}
