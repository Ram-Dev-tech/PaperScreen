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
    var showBookmarks by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showNotesForHighlight by remember { mutableStateOf<com.paperscreen.android.reader.data.HighlightEntity?>(null) }
    
    val bookmarks by viewModel.getBookmarks().collectAsState(initial = emptyList())
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    // We need to flow highlights per position identifier, but since we chunk them, 
    // it's easier to collect the current list of highlights when the position changes.
    // Instead of querying constantly in the UI, we'll let the composable render it.
    // Actually, let's just create a side-effect that queries highlights when contentPositions change.

    val currentSuccessState by rememberUpdatedState(state as? ReaderState.Success)
    var onStopFlush: (() -> Unit)? by remember { mutableStateOf(null) }

    var currentHighlights by remember { mutableStateOf<List<com.paperscreen.android.reader.data.HighlightEntity>>(emptyList()) }

    LaunchedEffect(currentSuccessState?.initialPosition) {
        val identifier = currentSuccessState?.initialPosition ?: return@LaunchedEffect
        viewModel.getHighlightsForPosition(identifier).collectLatest {
            currentHighlights = it
        }
    }

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
                    if (state is ReaderState.Success) {
                        Button(onClick = { showSearch = true }, modifier = Modifier.padding(end = 4.dp)) { Text("Search") }
                        Button(onClick = { showBookmarks = true }, modifier = Modifier.padding(end = 4.dp)) { Text("Marks") }
                    }
                    if (state is ReaderState.Success && (state as ReaderState.Success).fileType == "EPUB") {
                        Button(onClick = { showToc = true }, modifier = Modifier.padding(end = 4.dp)) { Text("TOC") }
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                                                
                                                val displayText = buildAnnotatedString {
                                                    append(text)
                                                    
                                                    // Render persistent highlights for this paragraph
                                                    // Since currentHighlights is bound to the document position, we need to map offsets.
                                                    // Actually, we are saving highlights with startIndex/endIndex relative to the whole chunk/chapter.
                                                    // We need to calculate the para's global start/end in the chunk to see if the highlight intersects.
                                                    
                                                    // But wait, in Phase 9H we changed HighlightEntity to have startIndex/endIndex relative to the chunk.
                                                    // We don't have the global offset of `para` easily unless we compute it.
                                                    // Let's compute it:
                                                }
                                                // Compute paragraph start offset by keeping a running total if we iterate.
                                                // Actually, it's easier to just match substrings if it's simple, or pass the start offset to the para.
                                                // Let's calculate para offsets before the loop:
                                                val paraOffsets = remember(chunk) {
                                                    val offsets = mutableListOf<Int>()
                                                    var current = 0
                                                    paragraphs.forEach { p ->
                                                        offsets.add(current)
                                                        current += p.length + 2 // +2 for "\n\n"
                                                    }
                                                    offsets
                                                }
                                                val paraStart = paraOffsets.getOrNull(paraIndex) ?: 0
                                                val paraEnd = paraStart + para.length
                                                
                                                val annotatedText = buildAnnotatedString {
                                                    append(text)
                                                    
                                                    // Apply persistent highlights
                                                    currentHighlights.forEach { highlight ->
                                                        val hStart = highlight.startIndex
                                                        val hEnd = highlight.endIndex
                                                        
                                                        // Check intersection with this paragraph
                                                        val intersectStart = maxOf(hStart, paraStart)
                                                        val intersectEnd = minOf(hEnd, paraEnd)
                                                        
                                                        if (intersectStart < intersectEnd) {
                                                            addStyle(
                                                                style = SpanStyle(
                                                                    background = androidx.compose.ui.graphics.Color(0xFF444444),
                                                                    color = androidx.compose.ui.graphics.Color(0xFFD8D6CF)
                                                                ),
                                                                start = intersectStart - paraStart,
                                                                end = intersectEnd - paraStart
                                                            )
                                                            
                                                            // Add string annotation so we can click it to open Notes
                                                            addStringAnnotation(
                                                                tag = "HIGHLIGHT",
                                                                annotation = highlight.id.toString(),
                                                                start = intersectStart - paraStart,
                                                                end = intersectEnd - paraStart
                                                            )
                                                        }
                                                    }

                                                    // Apply temporary selection highlight
                                                    if (isSelectedPara && selectionState != null) {
                                                        // selectionState has startIndex/endIndex relative to the paragraph!
                                                        addStyle(
                                                            style = SpanStyle(
                                                                background = MaterialTheme.colorScheme.onBackground,
                                                                color = MaterialTheme.colorScheme.background
                                                            ),
                                                            start = selectionState!!.startIndex,
                                                            end = selectionState!!.endIndex
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = annotatedText,
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
                                                            detectTapGestures(
                                                                onLongPress = { offset ->
                                                                    val layout = textLayoutResult ?: return@detectTapGestures
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
                                                                },
                                                                onTap = { offset ->
                                                                    val layout = textLayoutResult ?: return@detectTapGestures
                                                                    val charIndex = layout.getOffsetForPosition(offset)
                                                                    val annotations = annotatedText.getStringAnnotations(tag = "HIGHLIGHT", start = charIndex, end = charIndex)
                                                                    val highlightId = annotations.firstOrNull()?.item?.toLongOrNull()
                                                                    if (highlightId != null) {
                                                                        val h = currentHighlights.find { it.id == highlightId }
                                                                        if (h != null) {
                                                                            showNotesForHighlight = h
                                                                        }
                                                                    }
                                                                }
                                                            )
                                                        }
                                                )
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
                            onHighlight = {
                                val posIdentifier = if (currentState.fileType == "EPUB") {
                                    currentState.initialPosition // The locator JSON
                                } else {
                                    // For TXT, we saved contentPositions during chunking
                                    currentState.contentPositions.getOrNull(sel.chunkIndex) ?: "0"
                                }
                                
                                // In the new system, we save startIndex/endIndex relative to the whole chunk
                                val chunk = currentState.contentChunks.getOrNull(sel.chunkIndex) ?: ""
                                val paragraphs = chunk.split("\n\n")
                                var paraOffset = 0
                                for (i in 0 until sel.paraIndex) {
                                    paraOffset += paragraphs[i].length + 2
                                }
                                
                                viewModel.addHighlight(
                                    positionIdentifier = posIdentifier,
                                    startIndex = paraOffset + sel.startIndex,
                                    endIndex = paraOffset + sel.endIndex,
                                    selectedText = sel.word
                                )
                                selectionState = null
                            },
                            onMark = { 
                                viewModel.addBookmark("Marked word: ${sel.word}")
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

        if (showBookmarks) {
            BookmarkSheet(
                bookmarks = bookmarks,
                onBookmarkSelect = { pos ->
                    viewModel.jumpToPosition(pos)
                    showBookmarks = false
                },
                onDelete = { viewModel.deleteBookmark(it) },
                onDismiss = { showBookmarks = false }
            )
        }

        if (showSearch) {
            ReaderSearchSheet(
                results = searchResults,
                isSearching = isSearching,
                onSearch = { viewModel.searchBook(it) },
                onResultSelect = { res ->
                    viewModel.jumpToPosition(res.positionIdentifier)
                    showSearch = false
                },
                onDismiss = {
                    viewModel.clearSearch()
                    showSearch = false
                }
            )
        }

        if (showNotesForHighlight != null) {
            val h = showNotesForHighlight!!
            val notes by viewModel.getNotesForHighlight(h.id).collectAsState(initial = emptyList())
            NoteSheet(
                notes = notes,
                highlightedText = h.selectedText,
                onAddNote = { text -> viewModel.addNote(h.id, text) },
                onUpdateNote = { note, newText -> viewModel.updateNote(note, newText) },
                onDeleteNote = { viewModel.deleteNote(it) },
                onDeleteHighlight = {
                    viewModel.deleteHighlight(h)
                    showNotesForHighlight = null
                },
                onDismiss = { showNotesForHighlight = null }
            )
        }
    }
}
