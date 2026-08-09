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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.reader.parser.PdfReaderEngineFacade
import com.paperscreen.android.reader.settings.ReaderSettingsManager
import com.paperscreen.android.reader.settings.ReaderSettings
import com.paperscreen.android.reader.settings.readerDataStore
import com.paperscreen.android.dictionary.engine.DictionaryManager
import com.paperscreen.android.dictionary.model.DictionaryDefinition
import com.paperscreen.android.dictionary.ui.DictionaryPopup
import com.paperscreen.android.dictionary.ui.WordActionMenu
import androidx.compose.foundation.lazy.itemsIndexed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SelectionState(
    val word: String,
    val chunkIndex: Int,
    val paraIndex: Int,
    val startIndex: Int,
    val endIndex: Int,
    val anchorX: Int,
    val anchorY: Int
)

fun getWordBoundaries(text: String, index: Int): Pair<Int, Int>? {
    val breakIterator = java.text.BreakIterator.getWordInstance()
    breakIterator.setText(text)
    
    var start = breakIterator.preceding(index + 1)
    if (start == java.text.BreakIterator.DONE) start = 0
    
    var end = breakIterator.following(index)
    if (end == java.text.BreakIterator.DONE) end = text.length
    
    if (start < end) {
        val word = text.substring(start, end)
        if (word.any { it.isLetterOrDigit() }) {
            return Pair(start, end)
        }
    }
    return null
}

@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    val settingsManager = remember { ReaderSettingsManager(context.readerDataStore) }
    val settings by settingsManager.settingsFlow.collectAsState(initial = ReaderSettings())
    
    val dictionaryManager = remember { DictionaryManager(context) }
    var selectionState by remember { mutableStateOf<SelectionState?>(null) }
    var dictionaryPopupDefinitions by remember { mutableStateOf<List<DictionaryDefinition>?>(null) }
    val availableLanguages by dictionaryManager.getAvailableLanguages().collectAsState(initial = emptyList())
    var currentLanguageCode by remember { mutableStateOf("en") }

    val state by viewModel.state.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showToc by remember { mutableStateOf(false) }

    val currentSuccessState by rememberUpdatedState(state as? ReaderState.Success)
    var onStopFlush: (() -> Unit)? by remember { mutableStateOf(null) }

    LaunchedEffect(bookId) {
        dictionaryManager.ensureDefaultDictionaryInstalled()
        currentLanguageCode = dictionaryManager.getCurrentLanguage()
        viewModel.loadBook(bookId)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                onStopFlush?.invoke()
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
                    if (state is ReaderState.Success && (state as ReaderState.Success).fileType == "EPUB") {
                        Button(onClick = { showToc = true }, modifier = Modifier.padding(end = 8.dp)) { Text("TOC") }
                    }
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

                    onStopFlush = {
                        val s = currentSuccessState
                        if (s != null) {
                            val index = listState.firstVisibleItemIndex
                            val totalItems = listState.layoutInfo.totalItemsCount
                            val chunkProgress = if (totalItems <= 1) 1f else index.toFloat() / (totalItems - 1)
                            
                            val progress = if (s.fileType == "EPUB") {
                                if (s.chapterCount > 0) {
                                    (s.chapterIndex.toFloat() + chunkProgress) / s.chapterCount.toFloat()
                                } else 0f
                            } else {
                                chunkProgress
                            }
                            val finalProgress = if (progress >= 0.99f) 1.0f else progress
                            val posToSave = if (s.contentPositions.isNotEmpty()) {
                                s.contentPositions.getOrNull(index) ?: index.toString()
                            } else {
                                index.toString()
                            }
                            viewModel.saveReadingState(posToSave, finalProgress)
                        }
                    }

                    LaunchedEffect(listState) {
                        snapshotFlow { listState.firstVisibleItemIndex }
                            .collectLatest { index ->
                                delay(2000)
                                val totalItems = listState.layoutInfo.totalItemsCount
                                val chunkProgress = if (totalItems <= 1) 1f else index.toFloat() / (totalItems - 1)
                                
                                val progress = if (currentState.fileType == "EPUB") {
                                    if (currentState.chapterCount > 0) {
                                        (currentState.chapterIndex.toFloat() + chunkProgress) / currentState.chapterCount.toFloat()
                                    } else 0f
                                } else {
                                    chunkProgress
                                }
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
                                itemsIndexed(currentState.contentChunks.toList()) { chunkIndex, chunk ->
                                    val paragraphs = chunk.split("\n\n")
                                    Column {
                                        paragraphs.forEachIndexed { paraIndex, para ->
                                            val text = para.trim()
                                            if (text.isNotBlank()) {
                                                var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                                                var globalPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                                                val isSelectedPara = selectionState?.chunkIndex == chunkIndex && selectionState?.paraIndex == paraIndex
                                                val displayText = if (isSelectedPara && selectionState != null) {
                                                    buildAnnotatedString {
                                                        append(text)
                                                        addStyle(
                                                            style = SpanStyle(
                                                                background = MaterialTheme.colorScheme.onBackground,
                                                                color = MaterialTheme.colorScheme.background
                                                            ),
                                                            start = selectionState!!.startIndex,
                                                            end = selectionState!!.endIndex
                                                        )
                                                    }
                                                } else {
                                                    androidx.compose.ui.text.AnnotatedString(text)
                                                }

                                                Text(
                                                    text = displayText,
                                                    fontSize = settings.fontSize.sp,
                                                    fontFamily = fontFamily,
                                                    fontWeight = fontWeight,
                                                    lineHeight = (settings.fontSize * settings.lineSpacing).sp,
                                                    letterSpacing = settings.letterSpacing.sp,
                                                    textAlign = textAlign,
                                                    onTextLayout = { textLayoutResult = it },
                                                    modifier = Modifier
                                                        .padding(bottom = settings.paragraphSpacing.dp)
                                                        .onGloballyPositioned { layoutCoordinates ->
                                                            globalPosition = layoutCoordinates.positionInRoot()
                                                        }
                                                        .pointerInput(Unit) {
                                                            detectCustomLongPress { offset ->
                                                                val layout = textLayoutResult ?: return@detectCustomLongPress
                                                                val charIndex = layout.getOffsetForPosition(offset)
                                                                val wordBounds = getWordBoundaries(text, charIndex)
                                                                if (wordBounds != null) {
                                                                    val anchorX = (globalPosition.x + offset.x).roundToInt()
                                                                    val anchorY = (globalPosition.y + offset.y).roundToInt()
                                                                    selectionState = SelectionState(
                                                                        word = text.substring(wordBounds.first, wordBounds.second),
                                                                        chunkIndex = chunkIndex,
                                                                        paraIndex = paraIndex,
                                                                        startIndex = wordBounds.first,
                                                                        endIndex = wordBounds.second,
                                                                        anchorX = anchorX,
                                                                        anchorY = anchorY
                                                                    )
                                                                }
                                                            }
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (currentState.fileType == "EPUB") {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.loadEpubChapter(currentState.chapterIndex - 1) },
                                    enabled = currentState.chapterIndex > 0
                                ) {
                                    Text("< Prev")
                                }
                                Text("${currentState.chapterIndex + 1} / ${currentState.chapterCount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { viewModel.loadEpubChapter(currentState.chapterIndex + 1) },
                                    enabled = currentState.chapterIndex < currentState.chapterCount - 1
                                ) {
                                    Text("Next >")
                                }
                            }
                        }
                    }

                    selectionState?.let { sel ->
                        WordActionMenu(
                            anchorX = sel.anchorX,
                            anchorY = sel.anchorY,
                            onDictionary = {
                                coroutineScope.launch {
                                    dictionaryPopupDefinitions = dictionaryManager.lookup(sel.word, currentLanguageCode)
                                    selectionState = null
                                }
                            },
                            onHighlight = { selectionState = null },
                            onMark = { 
                                viewModel.addBookmark("Marked word: ${sel.word}")
                                selectionState = null 
                            },
                            onSearchGoogle = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://www.google.com/search?q=${sel.word}")
                                }
                                context.startActivity(intent)
                                selectionState = null
                            },
                            onCopy = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Copied Word", sel.word))
                                selectionState = null
                            },
                            onShare = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, sel.word)
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share word"))
                                selectionState = null
                            },
                            onDismiss = { selectionState = null }
                        )
                    }

                    dictionaryPopupDefinitions?.let { defs ->
                        DictionaryPopup(
                            definitions = defs,
                            availableLanguages = availableLanguages,
                            currentLanguageCode = currentLanguageCode,
                            onLanguageSelected = { lang ->
                                coroutineScope.launch {
                                    dictionaryManager.setCurrentLanguage(lang)
                                    currentLanguageCode = lang
                                }
                            },
                            onDismiss = { dictionaryPopupDefinitions = null }
                        )
                    }
                }
            }
        }
        
        if (showSettings) {
            ReaderSettingsSheet(
                settings = settings,
                onSettingsChanged = { coroutineScope.launch { settingsManager.updateSettings(it) } },
                onReset = { coroutineScope.launch { settingsManager.resetToDefault() } },
                onDismiss = { showSettings = false }
            )
        }
        
        if (showToc && state is ReaderState.Success) {
            val s = state as ReaderState.Success
            EpubTocSheet(
                toc = s.tableOfContents,
                currentChapterIndex = s.chapterIndex,
                onChapterSelect = { idx ->
                    viewModel.loadEpubChapter(idx)
                    showToc = false
                },
                onDismiss = { showToc = false }
            )
        }
    }
}
