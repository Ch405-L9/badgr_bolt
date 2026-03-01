package com.badgr.orbreader.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badgr.orbreader.ui.theme.ReaderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var wpm by remember { mutableStateOf(300) }
    var fontSize by remember { mutableStateOf(24) }
    var selectedColor by remember { mutableStateOf(ReaderColors.orpFocal) }

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // ── Reading Speed Selection ──────────────────────────────────
                Column {
                    Text("Reading Speed (WPM)", color = ReaderColors.textWarm, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { if (wpm > 50) wpm -= 25 }, colors = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal)) {
                            Text("-", fontSize = 20.sp)
                        }
                        Text("$wpm", color = ReaderColors.textWarm, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        OutlinedButton(onClick = { wpm += 25 }, colors = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal)) {
                            Text("+", fontSize = 20.sp)
                        }
                    }
                }
            }

            item {
                // ── Display Size ─────────────────────────────────────────────
                Column {
                    Text("Font Size (pt)", color = ReaderColors.textWarm, style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { fontSize = it.toInt() },
                        valueRange = 12f..64f,
                        colors = SliderDefaults.colors(thumbColor = ReaderColors.orpFocal, activeTrackColor = ReaderColors.orpFocal)
                    )
                    Text("${fontSize}pt", color = ReaderColors.textDimmed, fontSize = 14.sp)
                }
            }

            item {
                // ── ORP Color Palette ────────────────────────────────────────
                Column {
                    Text("ORP Highlight Color", color = ReaderColors.textWarm, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ColorChip(ReaderColors.orpFocal, selectedColor == ReaderColors.orpFocal) { selectedColor = it }
                        ColorChip(Color(0xFFE91E63), selectedColor == Color(0xFFE91E63)) { selectedColor = it }
                        ColorChip(Color(0xFF4CAF50), selectedColor == Color(0xFF4CAF50)) { selectedColor = it }
                        ColorChip(Color(0xFFFFC107), selectedColor == Color(0xFFFFC107)) { selectedColor = it }
                    }
                }
            }

            item {
                // ── File Support Section ─────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ReaderColors.cardSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Supported Formats", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FormatBadge("TXT")
                            FormatBadge("PDF")
                            FormatBadge("EPUB")
                        }
                    }
                }
            }

            item {
                // ── Pro Banner Stub ──────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    color = Color(0xFF2C2C2C),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Upgrade to Bolt Pro", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorChip(color: Color, isSelected: Boolean, onClick: (Color) -> Unit) {
    Surface(
        modifier = Modifier.size(40.dp),
        onClick = { onClick(color) },
        color = color,
        shape = CircleShape,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
    ) {}
}

@Composable
fun FormatBadge(label: String) {
    Surface(
        color = ReaderColors.badgrBlue.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            label, 
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
            color = ReaderColors.textWarm, 
            fontSize = 12.sp, 
            fontWeight = FontWeight.Bold
        )
    }
}
