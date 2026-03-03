package com.badgr.orbreader.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.billing.ProGate
import com.badgr.orbreader.data.local.ReadingSessionEntity
import com.badgr.orbreader.ui.theme.ReaderColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel = viewModel()) {
    val snapshot by vm.snapshot.collectAsState()
    val sessions by vm.sessions.collectAsState()

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Reading Stats", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->

        if (!ProGate.statsScreen) {
            // Free tier gate placeholder
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Stats is a Pro feature", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Upgrade to unlock reading analytics", color = ReaderColors.textDimmed)
                }
            }
            return@Scaffold
        }

        if (snapshot.totalSessions == 0) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No sessions yet", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Start reading to track your progress", color = ReaderColors.textDimmed)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Summary cards ─────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Best WPM",     "${snapshot.bestWpm}",          "wpm",  Modifier.weight(1f))
                    StatCard("Avg WPM",      "${snapshot.averageWpm}",       "wpm",  Modifier.weight(1f))
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Sessions",     "${snapshot.totalSessions}",    "",     Modifier.weight(1f))
                    StatCard("Total Time",   "${snapshot.totalReadingMins}", "min",  Modifier.weight(1f))
                }
            }
            item {
                StatCard(
                    "Total Words Read",
                    formatLargeNumber(snapshot.totalWordsRead),
                    "words",
                    Modifier.fillMaxWidth()
                )
            }

            // ── Recent sessions header ────────────────────────────────────
            item {
                Text(
                    "Recent Sessions",
                    color      = ReaderColors.textWarm,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(top = 8.dp)
                )
            }

            // ── Session rows ──────────────────────────────────────────────
            items(sessions.take(20)) { session ->
                SessionRow(session)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = ReaderColors.orpFocal.copy(alpha = 0.08f),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = ReaderColors.textDimmed, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = ReaderColors.textWarm, fontSize = 24.sp, fontWeight = FontWeight.Black)
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(unit, color = ReaderColors.orpFocal, fontSize = 12.sp,
                         modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: ReadingSessionEntity) {
    val dateStr = remember(session.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.timestamp))
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = ReaderColors.orpFocal.copy(alpha = 0.04f),
        shape    = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    session.bookTitle,
                    color    = ReaderColors.textWarm,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    dateStr,
                    color    = ReaderColors.textDimmed,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${session.avgWpm} WPM", color = ReaderColors.orpFocal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${session.wordsRead} words", color = ReaderColors.textDimmed, fontSize = 11.sp)
            }
        }
    }
}

private fun formatLargeNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000     -> "%.1fK".format(n / 1_000.0)
    else           -> n.toString()
}
