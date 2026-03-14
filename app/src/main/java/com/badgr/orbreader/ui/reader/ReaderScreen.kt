package com.badgr.orbreader.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.AchievementToastHost
import com.badgr.orbreader.ui.components.ChunkWordDisplay
import com.badgr.orbreader.ui.theme.ReaderColors
import com.badgr.orbreader.ui.theme.ReaderFonts

private val ORP_COLOR_LIST = listOf(
    Color(0xFF00CED1),
    Color(0xFF4CAF50),
    Color(0xFFFFC107),
    Color(0xFFE040FB),
    Color(0xFFE53935),
)

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
    val orpColorIndex   by viewModel.orpColorIndex.collectAsState()
    val fontSize        by viewModel.fontSize.collectAsState()
    val fontIndex       by viewModel.fontIndex.collectAsState()
    val chunkSize       by viewModel.chunkSize.collectAsState()
    val newAchievements by viewModel.newAchievements.collectAsState()

    val currentOrpColor   = ORP_COLOR_LIST.getOrElse(orpColorIndex) { ORP_COLOR_LIST[0] }
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
                        Text(
                            "${state.currentIndex + 1} / ${state.words.size}",
                            color = ReaderColors.textDimmed,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress   = { state.progress },
                            modifier   = Modifier.fillMaxWidth(),
                            color      = currentOrpColor,
                            trackColor = ReaderColors.guideLine
                        )
                        Spacer(Modifier.height(20.dp))

                        // ── WPM row ───────────────────────────────────────
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.skipSeconds(-10) }) {
                                Icon(Icons.Default.SkipPrevious, "Back 10s", tint = ReaderColors.textWarm)
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
                            IconButton(onClick = { viewModel.skipSeconds(10) }) {
                                Icon(Icons.Default.SkipNext, "Forward 10s", tint = ReaderColors.textWarm)
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
