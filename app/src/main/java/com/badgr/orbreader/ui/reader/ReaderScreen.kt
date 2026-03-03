package com.badgr.orbreader.ui.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.components.OrpWordDisplay
import com.badgr.orbreader.ui.theme.ReaderColors

private val ORP_COLORS = listOf(
    Color(0xFF00CED1),   // 0 cyan-teal (default)
    Color(0xFF4CAF50),   // 1 green
    Color(0xFFFFC107),   // 2 amber
    Color(0xFFE040FB),   // 3 purple
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(bookId) { viewModel.loadBook(bookId) }

    val state by viewModel.state.collectAsState()
    val bookTitle by viewModel.bookTitle.collectAsState()
    
    // Collect persistent user preferences
    val userPrefs by viewModel.userPrefs.collectAsState()

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
                        text = bookTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = ReaderColors.textWarm,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.saveProgress(); onBack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Reader",
                            tint = ReaderColors.textWarm
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ReaderColors.background
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ReaderColors.orpFocal)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(ReaderColors.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── RSVP Word Area ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Fixed Vertical Guide Lines
                Canvas(modifier = Modifier.size(width = 40.dp, height = 120.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    val lineLength = 15.dp.toPx()
                    val guideColor = ORP_COLORS.getOrElse(userPrefs.orpColorIndex) { ORP_COLORS[0] }
                    
                    drawLine(
                        color = guideColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, lineLength),
                        strokeWidth = strokeWidth
                    )
                    
                    drawLine(
                        color = guideColor,
                        start = Offset(size.width / 2, size.height - lineLength),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = strokeWidth
                    )
                }

                OrpWordDisplay(
                    word = state.currentWord,
                    fontSize = userPrefs.fontSize.sp, // NOW DYNAMIC
                    showOrpColor = userPrefs.showOrpColor, // NOW DYNAMIC
                    orpColor = ORP_COLORS.getOrElse(userPrefs.orpColorIndex) { ORP_COLORS[0] } // NOW DYNAMIC
                )
            }

            // ── Bottom Controls ────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ReaderColors.background,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${state.currentIndex + 1} / ${state.words.size}",
                        color = ReaderColors.textDimmed,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.skipSeconds(-5) }, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Default.SkipPrevious, "Back", tint = ReaderColors.textWarm, modifier = Modifier.size(36.dp))
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        LargeFloatingActionButton(
                            onClick = viewModel::togglePlayPause,
                            containerColor = ORP_COLORS.getOrElse(userPrefs.orpColorIndex) { ORP_COLORS[0] },
                            contentColor = ReaderColors.background,
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(onClick = { viewModel.skipSeconds(5) }, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Default.SkipNext, "Forward", tint = ReaderColors.textWarm, modifier = Modifier.size(36.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.adjustWpm(-25) },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.textWarm)
                        ) {
                            Text("− 25", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "${state.wpm} WPM",
                            style = MaterialTheme.typography.titleMedium,
                            color = ORP_COLORS.getOrElse(userPrefs.orpColorIndex) { ORP_COLORS[0] },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )

                        OutlinedButton(
                            onClick = { viewModel.adjustWpm(25) },
                            modifier = Modifier.width(100.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.textWarm)
                        ) {
                            Text("+ 25", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
