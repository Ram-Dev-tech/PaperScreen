package com.paperscreen.android.reader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
import com.paperscreen.android.reader.parser.TxtReaderEngineFacade
import com.paperscreen.android.reader.parser.EpubReaderEngineFacade
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    title: String,
    fileType: String,
    contentChunks: List<String>, // For TXT/EPUB
    pdfEngine: PdfReaderEngineFacade?,
    pageCount: Int,
    fontSize: Float,
    lineSpacing: Float,
    onSettingsClick: () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                },
                actions = {
                    Button(onClick = onSettingsClick) { Text("Settings") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (fileType == "PDF" && pdfEngine != null) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(pageCount) { index ->
                        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                        
                        LaunchedEffect(index) {
                            coroutineScope.launch {
                                bitmap = pdfEngine.renderPage(index, 2.0f)
                            }
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Page $index",
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            } else {
                // TXT or EPUB
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, 
                        end = 16.dp, 
                        top = 16.dp, 
                        bottom = 120.dp
                    )
                ) {
                    items(contentChunks) { chunk ->
                        Text(
                            text = chunk,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * lineSpacing).sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
