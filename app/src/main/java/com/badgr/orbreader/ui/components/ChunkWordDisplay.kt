package com.badgr.orbreader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.badgr.orbreader.ui.theme.ReaderColors
import com.badgr.orbreader.util.OrpEngine

/**
 * Displays a chunk of 1–4 words for chunk reading mode.
 *
 * When chunkSize == 1: delegates to OrpWordDisplay for full ORP rendering.
 * When chunkSize > 1: shows all words spaced evenly in a row.
 *   - First word gets ORP highlight treatment (bold, accent color).
 *   - Remaining words shown at normal weight, slightly dimmed.
 *   - This preserves the ORP focal anchor while giving peripheral context.
 */
@Composable
fun ChunkWordDisplay(
    words        : List<String>,
    fontSize     : TextUnit   = 48.sp,
    showOrpColor : Boolean    = true,
    orpColor     : Color      = ReaderColors.orpFocal,
    fontFamily   : FontFamily = FontFamily.Monospace
) {
    if (words.isEmpty()) return

    if (words.size == 1) {
        // Single word — use full ORP display
        OrpWordDisplay(
            word         = words[0],
            fontSize     = fontSize,
            showOrpColor = showOrpColor,
            orpColor     = orpColor,
            fontFamily   = fontFamily
        )
        return
    }

    // Multi-word chunk display
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        words.forEachIndexed { index, word ->
            if (index > 0) {
                Spacer(Modifier.width(12.dp))
            }
            if (index == 0) {
                // First word — ORP focal treatment
                val segments = OrpEngine.splitWordForOrp(word)
                val highlightColor = if (showOrpColor) orpColor else ReaderColors.textWarm
                val sideColor = ReaderColors.textWarm.copy(alpha = 0.9f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (segments.left.isNotEmpty()) {
                        Text(
                            text       = segments.left,
                            color      = sideColor,
                            fontSize   = fontSize,
                            fontFamily = fontFamily,
                            maxLines   = 1
                        )
                    }
                    Text(
                        text       = segments.orpChar,
                        color      = highlightColor,
                        fontSize   = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1
                    )
                    if (segments.right.isNotEmpty()) {
                        Text(
                            text       = segments.right,
                            color      = sideColor,
                            fontSize   = fontSize,
                            fontFamily = fontFamily,
                            maxLines   = 1
                        )
                    }
                }
            } else {
                // Context words — slightly smaller and dimmed
                val contextSize = fontSize * 0.85f
                Text(
                    text       = word,
                    color      = ReaderColors.textWarm.copy(alpha = 0.55f),
                    fontSize   = contextSize,
                    fontFamily = fontFamily,
                    maxLines   = 1
                )
            }
        }
    }
}
