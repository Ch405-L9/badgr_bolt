package com.badgr.orbreader.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.billing.ProGate
import com.badgr.orbreader.ui.theme.ReaderFonts
import com.badgr.orbreader.data.preferences.THEME_DARK
import com.badgr.orbreader.data.preferences.THEME_LIGHT
import com.badgr.orbreader.data.preferences.THEME_SYSTEM
import com.badgr.orbreader.ui.theme.ReaderColors

// 5 ORP colors — cyan, green, amber, purple, red
private val ORP_COLORS = listOf(
    Color(0xFF00CED1),   // 0 cyan-teal (default)
    Color(0xFF4CAF50),   // 1 green
    Color(0xFFFFC107),   // 2 amber
    Color(0xFFE040FB),   // 3 purple
    Color(0xFFE53935),   // 4 classic red
)

private val THEME_OPTIONS = listOf("System", "Light", "Dark")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val prefs by vm.prefs.collectAsState()

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            // ── Default Reading Speed ─────────────────────────────────────
            item {
                SettingSection(title = "Default Reading Speed") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick  = { vm.setWpm(prefs.defaultWpm - 25) },
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal),
                            modifier = Modifier.semantics { contentDescription = "Decrease WPM" }
                        ) { Text("−") }

                        Text(
                            "${prefs.defaultWpm} WPM",
                            color      = ReaderColors.textWarm,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Black
                        )

                        OutlinedButton(
                            onClick  = { vm.setWpm(prefs.defaultWpm + 25) },
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal),
                            modifier = Modifier.semantics { contentDescription = "Increase WPM" }
                        ) { Text("+") }
                    }
                }
            }

            // ── Font Size ─────────────────────────────────────────────────
            item {
                SettingSection(title = "Font Size  —  ${prefs.fontSize}pt") {
                    Slider(
                        value         = prefs.fontSize.toFloat(),
                        onValueChange = { vm.setFontSize(it.toInt()) },
                        valueRange    = 24f..60f,
                        colors = SliderDefaults.colors(
                            thumbColor       = ReaderColors.orpFocal,
                            activeTrackColor = ReaderColors.orpFocal
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = "Font size ${prefs.fontSize} points"
                        }
                    )
                }
            }

            // ── ORP Color Highlight ───────────────────────────────────────
            item {
                SettingSection(title = "ORP Highlight Color") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ORP_COLORS.forEachIndexed { idx, color ->
                            ColorChip(
                                color      = color,
                                isSelected = prefs.orpColorIndex == idx,
                                onClick    = { vm.setOrpColorIndex(idx) },
                                label      = "ORP color option $idx"
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Show ORP highlight",
                            color = ReaderColors.textWarm,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked         = prefs.showOrpColor,
                            onCheckedChange = { vm.setShowOrpColor(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ReaderColors.background,
                                checkedTrackColor = ReaderColors.orpFocal
                            )
                        )
                    }
                }
            }


            // ── Reader Font ───────────────────────────────────────────────
            item {
                SettingSection(title = "Reader Font") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderFonts.ALL.forEach { option ->
                            val selected = prefs.fontIndex == option.index
                            Surface(
                                onClick  = { vm.setFontIndex(option.index) },
                                modifier = Modifier.fillMaxWidth(),
                                color    = if (selected) ReaderColors.orpFocal.copy(alpha = 0.10f)
                                           else ReaderColors.background,
                                shape    = RoundedCornerShape(10.dp),
                                border   = androidx.compose.foundation.BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) ReaderColors.orpFocal
                                            else ReaderColors.guideLine
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text       = option.label,
                                            color      = if (selected) ReaderColors.orpFocal
                                                         else ReaderColors.textWarm,
                                            fontWeight = if (selected) FontWeight.Bold
                                                         else FontWeight.Normal,
                                            fontFamily = option.family,
                                            fontSize   = 15.sp
                                        )
                                        Text(
                                            text     = option.subtitle,
                                            color    = ReaderColors.textDimmed,
                                            fontSize = 11.sp
                                        )
                                    }
                                    if (option.isFixed) {
                                        Surface(
                                            color = ReaderColors.orpFocal.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "MONO",
                                                modifier      = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                color         = ReaderColors.orpFocal,
                                                fontSize      = 9.sp,
                                                fontWeight    = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Theme ─────────────────────────────────────────────────────
            item {
                SettingSection(title = "App Theme") {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        THEME_OPTIONS.forEachIndexed { idx, label ->
                            val selected = prefs.themeMode == idx
                            OutlinedButton(
                                onClick  = { vm.setThemeMode(idx) },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) ReaderColors.orpFocal.copy(alpha = 0.15f)
                                                     else Color.Transparent,
                                    contentColor   = if (selected) ReaderColors.orpFocal
                                                     else ReaderColors.textDimmed
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) ReaderColors.orpFocal
                                            else ReaderColors.textDimmed.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    label,
                                    fontSize   = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // ── Supported Formats ─────────────────────────────────────────
            item {
                SettingSection(title = "Supported Formats") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("TXT", "PDF", "EPUB", "DOCX", "IMAGE").forEach { fmt ->
                            Surface(
                                color = ReaderColors.orpFocal.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    fmt,
                                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color      = ReaderColors.orpFocal,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Pro Status ────────────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color    = if (ProGate.isPro) ReaderColors.orpFocal.copy(alpha = 0.12f)
                               else Color(0xFF2C2040),
                    shape    = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (ProGate.isPro) "BADGR Bolt Pro — Active" else "Upgrade to Bolt Pro",
                                color      = if (ProGate.isPro) ReaderColors.orpFocal else ReaderColors.textWarm,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (ProGate.isPro) "All features unlocked"
                                else "Stats, cloud sync, unlimited library",
                                color    = ReaderColors.textDimmed,
                                fontSize = 12.sp
                            )
                        }
                        if (!ProGate.isPro) {
                            OutlinedButton(
                                onClick = { /* billing — wired in AccountScreen */ },
                                colors  = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal)
                            ) { Text("Unlock") }
                        }
                    }
                }
            }

            // ── App Version ───────────────────────────────────────────────
            item {
                Text(
                    "BADGR Bolt v2.4.2 (build 5)",
                    color    = ReaderColors.textDimmed,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            color      = ReaderColors.textWarm,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ColorChip(color: Color, isSelected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .semantics { contentDescription = label },
        onClick  = onClick,
        color    = color,
        shape    = CircleShape,
        border   = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, Color.White) else null
    ) {}
}
