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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(if (state is ReaderState.Success) (state as ReaderState.Success).title else "Loading...") 
                },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val currentState = state) {
                is ReaderState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReaderState.Error -> {
                    Text(
                        text = "Error: ${currentState.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ReaderState.Success -> {
                    if (currentState.fileType == "PDF" && currentState.engine is PdfReaderEngineFacade) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentState.pageCount) { index ->
                                var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                
                                LaunchedEffect(index) {
                                    coroutineScope.launch {
                                        bitmap = (currentState.engine as PdfReaderEngineFacade).renderPage(index, 2.0f)
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
                            items(currentState.contentChunks) { chunk ->
                                Text(
                                    text = chunk,
                                    fontSize = 18.sp,
                                    lineHeight = 27.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
