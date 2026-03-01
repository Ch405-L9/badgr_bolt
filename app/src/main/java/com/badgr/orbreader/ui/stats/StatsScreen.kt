package com.badgr.orbreader.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badgr.orbreader.ui.theme.ReaderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Stats", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // ── Pro Banner Stub ──────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ReaderColors.orpFocal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(ReaderColors.orpFocal))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Unlock Pro Insights", color = ReaderColors.orpFocal, fontWeight = FontWeight.Bold)
                            Text("Get detailed reading heatmaps and cloud sync.", color = ReaderColors.textDimmed, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { /* Stub */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ReaderColors.orpFocal)
                        ) {
                            Text("Upgrade", color = ReaderColors.background)
                        }
                    }
                }
            }

            item {
                // ── Stats Grid ───────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Avg Speed", "450", "WPM", Modifier.weight(1f))
                        StatCard("Words Read", "12.4k", "total", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Time", "4.2", "hours", Modifier.weight(1f))
                        StatCard("Books", "12", "finished", Modifier.weight(1f))
                    }
                }
            }

            item {
                Text(
                    "Reading Progress",
                    color = ReaderColors.textWarm,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // ── Progress List Stubs ─────────────────────────────────────────
            items(3) { index ->
                val titles = listOf("The Great Gatsby", "Clean Code", "Android Internals")
                val progress = listOf(0.85f, 0.20f, 0.55f)
                
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(titles[index], color = ReaderColors.textWarm)
                        Text("${(progress[index] * 100).toInt()}%", color = ReaderColors.orpFocal)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress[index] },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = ReaderColors.orpFocal,
                        trackColor = ReaderColors.guideLine,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = ReaderColors.cardSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = ReaderColors.textDimmed, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = ReaderColors.textWarm, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(4.dp))
                Text(unit, color = ReaderColors.orpFocal, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}
