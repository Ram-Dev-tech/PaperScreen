package com.paperscreen.android.viewer.ui

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paperscreen.android.dictionary.engine.DictionaryManager
import com.paperscreen.android.dictionary.engine.WordNormalizer
import com.paperscreen.android.dictionary.ui.DictionaryPopup
import com.paperscreen.android.dictionary.ui.WordActionMenu
import com.paperscreen.android.viewer.engine.PdfViewerEngine
import com.paperscreen.android.viewer.engine.TxtViewerEngine
import com.paperscreen.android.viewer.model.ViewerState
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.BreakIterator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperViewerScreen(
    uriString: String,
    mimeType: String?,
    onBack: () -> Unit,
    onBridgeToReader: (Long) -> Unit
) {
    val viewModel: PaperViewerViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uriString) {
        viewModel.loadDocument(uriString, mimeType)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(viewModel.getFilename(), color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    Button(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) { Text("Back", color = MaterialTheme.colorScheme.background) }
                },
                actions = {
                    Button(onClick = { showInfoSheet = true }, modifier = Modifier.padding(end = 4.dp)) { Text("Info", color = MaterialTheme.colorScheme.background) }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = mimeType ?: "*/*"
                            putExtra(Intent.EXTRA_STREAM, viewModel.currentUri)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Document"))
                    }, modifier = Modifier.padding(end = 4.dp)) { Text("Share", color = MaterialTheme.colorScheme.background) }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(viewModel.currentUri, mimeType ?: "*/*")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open With"))
                    }, modifier = Modifier.padding(end = 4.dp)) { Text("Open With", color = MaterialTheme.colorScheme.background) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val currentState = state) {
                is ViewerState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                is ViewerState.Pdf -> PdfViewerContent(currentState.engine)
                is ViewerState.Txt -> TxtViewerContent(currentState.engine, viewModel)
                is ViewerState.Image -> ImageViewerContent(currentState.uri)
                is ViewerState.Error -> {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ViewerState.Unsupported -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unsupported document format",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (mimeType == "application/epub+zip" || uriString.endsWith(".epub", ignoreCase = true)) {
                            Button(onClick = {
                                viewModel.bridgeToReader { bookId ->
                                    onBridgeToReader(bookId)
                                }
                            }) {
                                Text("Open in Paper Reader", color = MaterialTheme.colorScheme.background)
                            }
                        } else {
                            Button(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(viewModel.currentUri, mimeType ?: "*/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open With"))
                            }) {
                                Text("Open With", color = MaterialTheme.colorScheme.background)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInfoSheet) {
        DocumentInfoSheet(
            filename = viewModel.getFilename(),
            mimeType = mimeType,
            fileSize = viewModel.getFileSize(),
            onDismissRequest = { showInfoSheet = false }
        )
    }
}

@Composable
fun PdfViewerContent(engine: PdfViewerEngine) {
    val pageCount = engine.pageCount
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(pageCount) { index ->
            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            LaunchedEffect(index) {
                bitmap = engine.renderPage(index, 2.0f) // Scale for better resolution
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(32.dp))
                }
            }
        }
    }
    
    // Page indicator
    val firstVisible = listState.firstVisibleItemIndex
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Text(
            text = "${firstVisible + 1} / $pageCount",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ImageViewerContent(uri: android.net.Uri) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                bitmap = BitmapFactory.decodeStream(stream)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Image Viewer",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

data class SelectionState(val word: String, val chunkIndex: Int, val startIndex: Int, val endIndex: Int, val anchorX: Float, val anchorY: Float)

@Composable
fun TxtViewerContent(engine: TxtViewerEngine, viewModel: PaperViewerViewModel) {
    val totalSize by engine.totalSize.collectAsState()
    val chunkSize = 4096
    val chunkCount = if (totalSize > 0) ((totalSize + chunkSize - 1) / chunkSize).toInt() else 1

    val context = LocalContext.current
    val dictionaryManager = remember { DictionaryManager(context) }
    var currentLanguageCode by remember { mutableStateOf("en") }
    val availableLanguages by dictionaryManager.getAvailableLanguages().collectAsState(initial = emptyList())
    
    var selectionState by remember { mutableStateOf<SelectionState?>(null) }
    var dictionaryPopupDefinitions by remember { mutableStateOf<List<com.paperscreen.android.dictionary.model.DictionaryDefinition>?>(null) }
    
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dictionaryManager.ensureDefaultDictionaryInstalled()
        currentLanguageCode = dictionaryManager.getCurrentLanguage()
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(chunkCount) { index ->
            var chunkText by remember { mutableStateOf<String?>(null) }
            var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            
            LaunchedEffect(index) {
                val (text, _) = engine.loadChunk((index * chunkSize).toLong(), chunkSize)
                chunkText = text
            }
            
            if (chunkText != null) {
                val annotatedString = buildAnnotatedString {
                    if (selectionState?.chunkIndex == index) {
                        val sel = selectionState!!
                        append(chunkText!!.substring(0, sel.startIndex))
                        withStyle(style = androidx.compose.ui.text.SpanStyle(
                            background = MaterialTheme.colorScheme.onBackground,
                            color = MaterialTheme.colorScheme.background
                        )) {
                            append(chunkText!!.substring(sel.startIndex, sel.endIndex))
                        }
                        append(chunkText!!.substring(sel.endIndex))
                    } else {
                        append(chunkText!!)
                    }
                }

                Text(
                    text = annotatedString,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge,
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    val layout = textLayoutResult ?: return@detectTapGestures
                                    val charIndex = layout.getOffsetForPosition(offset)
                                    val text = chunkText!!
                                    
                                    val iterator = BreakIterator.getWordInstance()
                                    iterator.setText(text)
                                    
                                    var start = iterator.preceding(charIndex)
                                    if (start == BreakIterator.DONE) start = 0
                                    var end = iterator.following(charIndex)
                                    if (end == BreakIterator.DONE) end = text.length
                                    
                                    val word = text.substring(start, end).trim { it.isWhitespace() || it.isISOControl() }
                                    
                                    if (word.isNotBlank() && word.any { it.isLetter() }) {
                                        val anchorX = offset.x
                                        val anchorY = offset.y
                                        selectionState = SelectionState(
                                            word = word,
                                            chunkIndex = index,
                                            startIndex = start,
                                            endIndex = end,
                                            anchorX = anchorX,
                                            anchorY = anchorY
                                        )
                                    }
                                },
                                onTap = {
                                    selectionState = null
                                }
                            )
                        }
                )
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(16.dp))
            }
        }
    }

    selectionState?.let { sel ->
        WordActionMenu(
            anchorX = sel.anchorX.toInt(),
            anchorY = sel.anchorY.toInt(),
            onDictionary = {
                coroutineScope.launch {
                    val normalized = WordNormalizer.normalize(sel.word)
                    dictionaryPopupDefinitions = dictionaryManager.lookup(normalized, currentLanguageCode)
                    selectionState = null
                }
            },
            onHighlight = { /* Not supported in Viewer */ selectionState = null },
            onMark = { /* Not supported in Viewer */ selectionState = null },
            onSearchGoogle = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
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
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, sel.word)
                }
                context.startActivity(Intent.createChooser(intent, "Share Word"))
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
            onLanguageSelected = { newLang ->
                coroutineScope.launch {
                    dictionaryManager.setCurrentLanguage(newLang)
                    currentLanguageCode = newLang
                    selectionState?.let { sel -> 
                        val normalized = WordNormalizer.normalize(sel.word)
                        dictionaryPopupDefinitions = dictionaryManager.lookup(normalized, newLang)
                    }
                }
            },
            onDismiss = { dictionaryPopupDefinitions = null }
        )
    }
}
