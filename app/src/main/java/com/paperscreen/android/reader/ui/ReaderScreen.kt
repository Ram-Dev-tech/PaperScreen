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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val settings by viewModel.settingsState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // Backgrounding logic handles save automatically via snapshotFlow below
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
                },
                actions = {
                    Button(onClick = { showSettings = true }) { Text("Aa") }
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
                        val hMargin = when (settings.margins) {
                            "Narrow" -> 8.dp
                            "Wide" -> 32.dp
                            else -> 16.dp
                        }
                        val maxWidth = when (settings.textWidth) {
                            "Narrow" -> 400.dp
                            "Comfortable" -> 600.dp
                            else -> 10000.dp
                        }
                        val fontFamily = when (settings.fontFamily) {
                            "Serif" -> FontFamily.Serif
                            "Monospace" -> FontFamily.Monospace
                            else -> FontFamily.SansSerif
                        }
                        val fontWeight = when (settings.fontWeight) {
                            "Medium" -> FontWeight.Medium
                            "Bold" -> FontWeight.Bold
                            else -> FontWeight.Normal
                        }
                        val textAlign = when (settings.alignment) {
                            "Center" -> TextAlign.Center
                            "Right" -> TextAlign.Right
                            "Justify" -> TextAlign.Justify
                            else -> TextAlign.Left
                        }

                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .widthIn(max = maxWidth),
                                state = listState,
                                contentPadding = PaddingValues(
                                    start = hMargin, 
                                    end = hMargin, 
                                    top = 16.dp, 
                                    bottom = 120.dp
                                )
                            ) {
                                items(currentState.contentChunks) { chunk ->
                                    // Treat double newlines as paragraphs manually
                                    val paragraphs = chunk.split("\n\n")
                                    Column {
                                        paragraphs.forEachIndexed { i, para ->
                                            if (para.isNotBlank()) {
                                                Text(
                                                    text = para.trim(),
                                                    fontSize = settings.fontSize.sp,
                                                    fontFamily = fontFamily,
                                                    fontWeight = fontWeight,
                                                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                                                    letterSpacing = settings.letterSpacing.sp,
                                                    textAlign = textAlign,
                                                    modifier = Modifier.padding(bottom = if (i == paragraphs.size - 1) settings.paragraphSpacing.dp else settings.paragraphSpacing.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showSettings) {
            ReaderSettingsSheet(
                settings = settings,
                onSettingsChanged = { viewModel.updateSettings(it) },
                onReset = { viewModel.resetSettingsToDefault() },
                onDismiss = { showSettings = false }
            )
        }
    }
}
