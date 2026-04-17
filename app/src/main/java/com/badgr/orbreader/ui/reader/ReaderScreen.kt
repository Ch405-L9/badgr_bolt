package com.badgr.orbreader.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.AchievementToastHost
import com.badgr.orbreader.ui.components.ChunkWordDisplay
import com.badgr.orbreader.ui.theme.ColorBlindness
import com.badgr.orbreader.ui.theme.ReaderColors
import com.badgr.orbreader.ui.theme.ReaderFonts

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    bookId   : String,
    onBack   : () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }

    val state               by viewModel.state.collectAsState()
    val bookTitle           by viewModel.bookTitle.collectAsState()
    val showOrp             by viewModel.showOrpColor.collectAsState()
    val orpColorIndex       by viewModel.orpColorIndex.collectAsState()
    val fontSize            by viewModel.fontSize.collectAsState()
    val fontIndex           by viewModel.fontIndex.collectAsState()
    val chunkSize           by viewModel.chunkSize.collectAsState()
    val newAchievements     by viewModel.newAchievements.collectAsState()
    val colorBlindnessMode  by viewModel.colorBlindnessMode.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val totalChapters       by viewModel.totalChapters.collectAsState()

    val haptic = LocalHapticFeedback.current
    val orpColorList      = ColorBlindness.getOrpColors(colorBlindnessMode)
    val currentOrpColor   = orpColorList.getOrElse(orpColorIndex) { orpColorList[0] }
    val currentFontFamily = ReaderFonts.fromIndex(fontIndex)

    // Get current chunk of words to display
    val currentChunk = remember(state.currentIndex, chunkSize) {
        viewModel.getCurrentChunk()
    }

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
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ReaderColors.textWarm)
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .background(ReaderColors.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Word display area ─────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 40.dp, height = 120.dp)) {
                        val sw = 2.dp.toPx()
                        val ll = 15.dp.toPx()
                        drawLine(currentOrpColor, Offset(size.width / 2, 0f), Offset(size.width / 2, ll), sw)
                        drawLine(currentOrpColor, Offset(size.width / 2, size.height - ll), Offset(size.width / 2, size.height), sw)
                    }
                    ChunkWordDisplay(
                        words        = currentChunk,
                        fontSize     = fontSize.sp,
                        showOrpColor = showOrp,
                        orpColor     = currentOrpColor,
                        fontFamily   = currentFontFamily
                    )
                }

                // ── Controls ──────────────────────────────────────────────
                Surface(
                    modifier       = Modifier.fillMaxWidth(),
                    color          = ReaderColors.background,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier            = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "${state.currentIndex + 1} / ${state.words.size}",
                                color = ReaderColors.textDimmed,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (totalChapters > 1) {
                                Text(
                                    "Ch ${currentChapterIndex + 1} / $totalChapters",
                                    color    = currentOrpColor.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress   = { state.progress },
                            modifier   = Modifier.fillMaxWidth(),
                            color      = currentOrpColor,
                            trackColor = ReaderColors.guideLine
                        )
                        Spacer(Modifier.height(20.dp))

                        // ── WPM row ───────────────────────────────────────
                        // Tap skip buttons: ±10 words  |  Long-press: ±1 chapter
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .combinedClickable(
                                        onClick     = { viewModel.skipWords(-10) },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.skipChapter(-1)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Back 10 words (hold: prev chapter)",
                                    tint = ReaderColors.textWarm
                                )
                            }
                            IconButton(onClick = { viewModel.adjustWpm(-25) }) {
                                Icon(Icons.Default.SkipPrevious, "-25 WPM", tint = ReaderColors.textDimmed)
                            }
                            FloatingActionButton(
                                onClick        = { viewModel.togglePlayPause() },
                                containerColor = currentOrpColor,
                                contentColor   = ReaderColors.background
                            ) {
                                Icon(
                                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    if (state.isPlaying) "Pause" else "Play"
                                )
                            }
                            IconButton(onClick = { viewModel.adjustWpm(25) }) {
                                Icon(Icons.Default.SkipNext, "+25 WPM", tint = ReaderColors.textDimmed)
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .combinedClickable(
                                        onClick     = { viewModel.skipWords(10) },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.skipChapter(1)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Forward 10 words (hold: next chapter)",
                                    tint = ReaderColors.textWarm
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${state.wpm} WPM",
                            color = currentOrpColor,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = ReaderColors.guideLine)
                        Spacer(Modifier.height(12.dp))

                        // ── Chunk size row ────────────────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                "Words at a time",
                                color    = ReaderColors.textDimmed,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick  = { viewModel.adjustChunkSize(-1) },
                                enabled  = chunkSize > 1
                            ) {
                                Text(
                                    "−",
                                    color      = if (chunkSize > 1) currentOrpColor else ReaderColors.guideLine,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                color  = currentOrpColor.copy(alpha = 0.12f),
                                shape  = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "$chunkSize",
                                    color      = currentOrpColor,
                                    fontSize   = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick  = { viewModel.adjustChunkSize(1) },
                                enabled  = chunkSize < 4
                            ) {
                                Text(
                                    "+",
                                    color      = if (chunkSize < 4) currentOrpColor else ReaderColors.guideLine,
                                    fontSize   = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            AchievementToastHost(
                newAchievementIds = newAchievements,
                onConsumed        = viewModel::consumeAchievements,
                modifier          = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
