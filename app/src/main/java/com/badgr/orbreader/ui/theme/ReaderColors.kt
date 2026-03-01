package com.badgr.orbreader.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Enhanced color palette for the BADGR Bolt RSVP engine.
 * Based on color-psychology research for calm focus and retention.
 */
object ReaderColors {
    // Deep dark background for high contrast/low strain
    val background = Color(0xFF121212)
    
    // Warm off-white for text to reduce blue light strain (WCAG compliant)
    val textWarm   = Color(0xFFE8E2D0)
    
    // Dimmed text for secondary metadata
    val textDimmed = Color(0xFF9E9E9E)
    
    // ORP Focal Color: Cyan/Teal (Research-backed for calm focus, replaces Red)
    val orpFocal   = Color(0xFF00CED1) 
    
    // BADGR Blue: Primary brand accent for buttons and progress
    val badgrBlue  = Color(0xFF0D1BFF)
    
    // Subtle guide line color for eye anchoring
    val guideLine  = Color(0xFF333333)
    
    // Progress bar color
    val progressBar = Color(0xFF00CED1)

    // Stats Card Backgrounds
    val cardSurface = Color(0xFF1E1E1E)
}
