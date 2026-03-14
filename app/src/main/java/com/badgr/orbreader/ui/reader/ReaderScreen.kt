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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.AchievementToastHost
import com.badgr.orbreader.ui.theme.ReaderFonts
import com.badgr.orbreader.ui.components.OrpWordDisplay
import com.badgr.orbreader.ui.theme.ReaderColors

// Must match the ORP_COLORS list in SettingsScreen exactly
private val ORP_COLOR_LIST = listOf(
    Color(0xFF00CED1),  // 0 cyan-teal
    Color(0xFF4CAF50),  // 1 green
    Color(0xFFFFC107),  // 2 amber
    Color(0xFFE040FB),  // 3 purple
    Color(0xFFE53935),  // 4 red
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
    val newAchievements by viewModel.newAchievements.collectAsState()

    val currentOrpColor  = ORP_COLOR_LIST.getOrElse(orpColorIndex) { ORP_COLOR_LIST[0] }
    val currentFontFamily = ReaderFonts.fromIndex(fontIndex)

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
                Box(
                    modifier         = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(width = 40.dp, height = 120.dp)) {
                        val sw = 2.dp.toPx()
                        val ll = 15.dp.toPx()
                        drawLine(currentOrpColor, Offset(size.width / 2, 0f),        Offset(size.width / 2, ll),              sw)
                        drawLine(currentOrpColor, Offset(size.width / 2, size.height - ll), Offset(size.width / 2, size.height), sw)
                    }
                    OrpWordDisplay(
                        word         = state.currentWord,
                        fontSize     = fontSize.sp,
                        showOrpColor = showOrp,
                        orpColor     = currentOrpColor,
                        fontFamily   = currentFontFamily
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
                        Spacer(Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.skipSeconds(-10) }) {
                                Icon(Icons.Default.SkipPrevious, "Back 10s",  tint = ReaderColors.textWarm)
                            }
                            IconButton(onClick = { viewModel.adjustWpm(-25) }) {
                                Icon(Icons.Default.SkipPrevious, "-25 WPM",   tint = ReaderColors.textDimmed)
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
                                Icon(Icons.Default.SkipNext, "+25 WPM",    tint = ReaderColors.textDimmed)
                            }
                            IconButton(onClick = { viewModel.skipSeconds(10) }) {
                                Icon(Icons.Default.SkipNext, "Forward 10s", tint = ReaderColors.textWarm)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${state.wpm} WPM",
                            color = currentOrpColor,
                            style = MaterialTheme.typography.labelMedium
                        )
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
