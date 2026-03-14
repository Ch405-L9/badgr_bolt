package com.badgr.orbreader.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.AchievementToastHost
import com.badgr.orbreader.ui.components.OrpWordDisplay
import com.badgr.orbreader.ui.theme.ReaderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId   : String,
    onBack   : () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }

    val state           by viewModel.state.collectAsState()
    val bookTitle       by viewModel.bookTitle.collectAsState()
    val showOrp         by viewModel.showOrpColor.collectAsState()
    val newAchievements by viewModel.newAchievements.collectAsState()

    BackHandler {
        viewModel.saveProgress()
        onBack()
    }

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text     = bookTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color    = ReaderColors.textWarm,
                        style    = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.saveProgress(); onBack() }) {
                        Icon(
                            imageVector        = Icons.Default.Close,
                            contentDescription = "Close Reader",
                            tint               = ReaderColors.textWarm
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ReaderColors.orpFocal)
            }
            return@Scaffold
        }

        // ── Main layout with toast overlay ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Reading content ───────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .background(ReaderColors.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier         = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 40.dp, height = 120.dp)) {
                        val strokeWidth = 2.dp.toPx()
                        val lineLength  = 15.dp.toPx()
                        drawLine(
                            color       = ReaderColors.orpFocal,
                            start       = Offset(size.width / 2, 0f),
                            end         = Offset(size.width / 2, lineLength),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color       = ReaderColors.orpFocal,
                            start       = Offset(size.width / 2, size.height - lineLength),
                            end         = Offset(size.width / 2, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    OrpWordDisplay(
                        word         = state.currentWord,
                        fontSize     = 52.sp,
                        showOrpColor = showOrp
                    )
                }

                Surface(
                    modifier       = Modifier.fillMaxWidth(),
                    color          = ReaderColors.background,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier            = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text  = "${state.currentIndex + 1} / ${state.words.size}",
                            color = ReaderColors.textDimmed,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress   = { state.progress },
                            modifier   = Modifier.fillMaxWidth(),
                            color      = ReaderColors.progressBar,
                            trackColor = ReaderColors.guideLine
                        )
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.skipSeconds(-10) }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Back 10s",
                                     tint = ReaderColors.textWarm)
                            }
                            IconButton(onClick = { viewModel.adjustWpm(-25) }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "-25 WPM",
                                     tint = ReaderColors.textDimmed)
                            }
                            FloatingActionButton(
                                onClick        = { viewModel.togglePlayPause() },
                                containerColor = ReaderColors.orpFocal,
                                contentColor   = ReaderColors.background
                            ) {
                                Icon(
                                    imageVector        = if (state.isPlaying) Icons.Default.Pause
                                                         else Icons.Default.PlayArrow,
                                    contentDescription = if (state.isPlaying) "Pause" else "Play"
                                )
                            }
                            IconButton(onClick = { viewModel.adjustWpm(25) }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "+25 WPM",
                                     tint = ReaderColors.textDimmed)
                            }
                            IconButton(onClick = { viewModel.skipSeconds(10) }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Forward 10s",
                                     tint = ReaderColors.textWarm)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = "${state.wpm} WPM",
                            color = ReaderColors.orpFocal,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // ── Achievement toast overlay — sits above everything ─────────
            AchievementToastHost(
                newAchievementIds = newAchievements,
                onConsumed        = viewModel::consumeAchievements,
                modifier          = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
