package com.badgr.orbreader.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.OrpWordDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }

    val state     by viewModel.state.collectAsState()
    val bookTitle by viewModel.bookTitle.collectAsState()
    val showOrp   by viewModel.showOrpColor.collectAsState()

    BackHandler {
        viewModel.saveProgress()
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(bookTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.saveProgress(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.words.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No words found for this book.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.weight(0.5f))

            // ── ORP word display ──────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Guide line — thin blue rule at vertical center
                Canvas(Modifier.fillMaxSize()) {
                    drawLine(
                        color       = Color(0xFF0D1BFF),
                        start       = Offset(0f, size.height / 2f),
                        end         = Offset(size.width, size.height / 2f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                OrpWordDisplay(
                    word         = state.currentWord,
                    fontSize     = 48.sp,
                    showOrpColor = showOrp
                )
            }

            Spacer(Modifier.weight(0.5f))

            // ── Progress ──────────────────────────────────────────────────
            Column(Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("${state.currentIndex + 1} / ${state.words.size}",
                         style = MaterialTheme.typography.labelSmall)
                    Text("${state.wpm} WPM",
                         style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Controls ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { viewModel.adjustWpm(-25) }) { Text("− Speed") }
                FloatingActionButton(onClick = viewModel::togglePlayPause) {
                    Icon(Icons.Filled.PlayArrow,
                         contentDescription = if (state.isPlaying) "Pause" else "Play")
                }
                OutlinedButton(onClick = { viewModel.adjustWpm(+25) }) { Text("+ Speed") }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
