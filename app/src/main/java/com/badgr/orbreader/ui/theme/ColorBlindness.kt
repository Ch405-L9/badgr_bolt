package com.badgr.orbreader.ui.theme

import androidx.compose.ui.graphics.Color

object ColorBlindness {
    const val NORMAL       = 0
    const val DEUTERANOPIA = 1  // Green weakness (most common ~6% of males)
    const val PROTANOPIA   = 2  // Red weakness
    const val TRITANOPIA   = 3  // Blue-yellow weakness (rare)
    const val HIGH_CONTRAST = 4 // Universal max-contrast

    val LABELS = listOf("None", "Deuteranopia", "Protanopia", "Tritanopia", "High Contrast")

    // ORP color palettes optimised per vision type (Bang Wong / IBM palette)
    private val NORMAL_COLORS = listOf(
        Color(0xFF00CED1), Color(0xFF4CAF50), Color(0xFFFFC107),
        Color(0xFFE040FB), Color(0xFFE53935)
    )
    private val DEUTERANOPIA_COLORS = listOf(
        Color(0xFF0072B2), Color(0xFFE69F00), Color(0xFF56B4E9),
        Color(0xFFF0E442), Color(0xFFCC79A7)
    )
    private val PROTANOPIA_COLORS = listOf(
        Color(0xFF0072B2), Color(0xFFE69F00), Color(0xFF56B4E9),
        Color(0xFFF0E442), Color(0xFF999999)
    )
    private val TRITANOPIA_COLORS = listOf(
        Color(0xFFD55E00), Color(0xFF009E73), Color(0xFFCC79A7),
        Color(0xFF000000), Color(0xFFE69F00)
    )
    private val HIGH_CONTRAST_COLORS = listOf(
        Color(0xFFFFFFFF), Color(0xFFFFFF00), Color(0xFF00FFFF),
        Color(0xFFFF00FF), Color(0xFFFF6600)
    )

    fun getOrpColors(mode: Int): List<Color> = when (mode) {
        DEUTERANOPIA  -> DEUTERANOPIA_COLORS
        PROTANOPIA    -> PROTANOPIA_COLORS
        TRITANOPIA    -> TRITANOPIA_COLORS
        HIGH_CONTRAST -> HIGH_CONTRAST_COLORS
        else          -> NORMAL_COLORS
    }
}
