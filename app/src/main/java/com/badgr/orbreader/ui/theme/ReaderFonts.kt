package com.badgr.orbreader.ui.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * BADGR Bolt Reader Font Registry
 *
 * Current build uses system font family approximations.
 * Full downloadable fonts (JetBrains Mono, Literata, Merriweather,
 * Atkinson Hyperlegible, Open Sans) will be wired at 2.5.x.
 *
 * RSVP note: Monospace fonts (index 0, 1) keep the ORP focal point
 * visually stable — equal character widths prevent horizontal jumping.
 * Serif/sans fonts (index 2-5) feel warmer for long reading sessions.
 *
 * Index 0  System Mono       — default, best ORP stability
 * Index 1  JetBrains Mono    — wider mono glyphs (system mono now, full font at 2.5.x)
 * Index 2  Literata          — purpose-built reading serif (system serif now)
 * Index 3  Merriweather      — high x-height serif (system serif now)
 * Index 4  Atkinson          — max clarity sans (system sans now)
 * Index 5  Open Sans         — low cognitive load sans (system sans now)
 */

data class ReaderFontOption(
    val index   : Int,
    val label   : String,
    val subtitle: String,
    val isFixed : Boolean,
    val family  : FontFamily
)

object ReaderFonts {

    val ALL = listOf(
        ReaderFontOption(0, "System Mono",  "Best ORP stability",          true,  FontFamily.Monospace),
        ReaderFontOption(1, "JetBrains",    "Wider glyphs, less strain",   true,  FontFamily.Monospace),
        ReaderFontOption(2, "Literata",     "Purpose-built for reading",   false, FontFamily.Serif),
        ReaderFontOption(3, "Merriweather", "High x-height, easy on eyes", false, FontFamily.Serif),
        ReaderFontOption(4, "Atkinson",     "Max clarity, focus-optimised",false, FontFamily.SansSerif),
        ReaderFontOption(5, "Open Sans",    "Clean, low cognitive load",   false, FontFamily.SansSerif)
    )

    fun fromIndex(index: Int): FontFamily =
        ALL.getOrElse(index) { ALL[0] }.family
}
