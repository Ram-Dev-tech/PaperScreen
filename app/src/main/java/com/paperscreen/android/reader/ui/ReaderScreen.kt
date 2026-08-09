package com.paperscreen.android.reader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Ensure state is saved when leaving or backgrounding
                // Real save logic handled via debounced snapshotFlow below, but we could flush here.
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                    val initialIndex = currentState.initialPosition.toIntOrNull() ?: 0
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

                    LaunchedEffect(listState) {
                        snapshotFlow { listState.firstVisibleItemIndex }
                            .collectLatest { index ->
                                delay(2000)
                                val totalItems = listState.layoutInfo.totalItemsCount
                                val progress = if (totalItems <= 1) 1f else index.toFloat() / (totalItems - 1)
                                val finalProgress = if (progress >= 0.99f) 1.0f else progress
                                
                                val posToSave = if (currentState.contentPositions.isNotEmpty()) {
                                    currentState.contentPositions.getOrNull(index) ?: index.toString()
                                } else {
                                    index.toString()
                                }
                                viewModel.saveReadingState(posToSave, finalProgress)
                            }
                    }

                    if (currentState.fileType == "PDF" && currentState.engine is PdfReaderEngineFacade) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
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
                            state = listState,
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
