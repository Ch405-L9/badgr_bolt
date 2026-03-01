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
 * Centered RSVP/ORP word display.
 * 
 * This component ensures the Optimal Recognition Point (ORP) character 
 * is always positioned at the exact horizontal center of the container.
 * We use Monospaced font and fixed-width side boxes to anchor the eye.
 */
@Composable
fun OrpWordDisplay(
    word: String,
    fontSize: TextUnit = 48.sp,
    showOrpColor: Boolean = true,
    fontFamily: FontFamily = FontFamily.Monospace // Monospace is critical for perfect alignment
) {
    val segments = OrpEngine.splitWordForOrp(word)
    
    // Use Cyan/Teal for focus per BADGR Bolt UX requirements
    val orpHighlight = if (showOrpColor) ReaderColors.orpFocal else ReaderColors.textWarm
    val sideColor    = ReaderColors.textWarm.copy(alpha = 0.8f)

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Left Segment: Anchored to the right of this box (adjacent to ORP)
        Box(
            modifier         = Modifier.weight(1f), 
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text       = segments.left,
                color      = sideColor,
                fontSize   = fontSize,
                fontFamily = fontFamily,
                maxLines   = 1
            )
        }

        // The Anchor: The ORP character itself, exactly in the middle
        Text(
            text       = segments.orpChar,
            color      = orpHighlight,
            fontSize   = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            maxLines   = 1
        )

        // Right Segment: Anchored to the left of this box (adjacent to ORP)
        Box(
            modifier         = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text       = segments.right,
                color      = sideColor,
                fontSize   = fontSize,
                fontFamily = fontFamily,
                maxLines   = 1
            )
        }
    }
}
