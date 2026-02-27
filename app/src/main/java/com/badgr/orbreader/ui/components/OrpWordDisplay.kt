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
import com.badgr.orbreader.util.OrpEngine

private val ORP_BLUE    = Color(0xFF0D1BFF)
private val LEFT_GRAY   = Color(0xFF808080)
private val RIGHT_WHITE = Color(0xFFFFFFFF)

@Composable
fun OrpWordDisplay(
    word: String,
    fontSize: TextUnit = 48.sp,
    showOrpColor: Boolean = true
) {
    val segments   = OrpEngine.splitWordForOrp(word)
    val fontFamily = FontFamily.Monospace
    val leftColor  = if (showOrpColor) LEFT_GRAY  else RIGHT_WHITE
    val orpColor   = if (showOrpColor) ORP_BLUE   else RIGHT_WHITE

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(200.dp), contentAlignment = Alignment.CenterEnd) {
            Text(segments.left, color = leftColor, fontSize = fontSize, fontFamily = fontFamily)
        }
        Text(segments.orpChar, color = orpColor, fontSize = fontSize,
             fontFamily = fontFamily, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.width(200.dp), contentAlignment = Alignment.CenterStart) {
            Text(segments.right, color = RIGHT_WHITE, fontSize = fontSize, fontFamily = fontFamily)
        }
    }
}
