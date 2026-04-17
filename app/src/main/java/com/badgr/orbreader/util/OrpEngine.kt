package com.badgr.orbreader.util

/**
 * BADGR Bolt RSVP Engine - Precision ORP Calculation.
 * 
 * The Optimal Recognition Point (ORP) is calculated to minimize saccadic 
 * eye movements. For most words, this is slightly to the left of the center.
 */
object OrpEngine {

    /**
     * Returns the 0-based index of the focal character (ORP).
     * Refined thresholds based on standard RSVP research.
     */
    fun getOrpIndex(word: String): Int {
        val length = word.length
        return when {
            length <= 1  -> 0
            length <= 5  -> 1
            length <= 9  -> 2
            length <= 13 -> 3
            else         -> 4
        }
    }

    /**
     * Strips leading and trailing punctuation while preserving internal 
     * punctuation (e.g. "don't").
     */
    fun stripPunctuation(word: String): Triple<String, String, String> {
        val leading = word.takeWhile { !it.isLetterOrDigit() }
        val trailing = word.takeLastWhile { !it.isLetterOrDigit() }
        val clean = word.removePrefix(leading).removeSuffix(trailing)
        return Triple(clean, leading, trailing)
    }

    /**
     * Returns true if the word ends with sentence-ending punctuation.
     */
    fun hasSentenceEndingPunctuation(word: String): Boolean {
        return word.lastOrNull() in setOf('.', '?', '!')
    }

    /**
     * Returns true if the word ends with clause-separating punctuation.
     */
    fun hasClausePunctuation(word: String): Boolean {
        return word.lastOrNull() in setOf(',', ';', ':')
    }

    /**
     * Splits a raw word into ORP segments. 
     * Handles long word hyphenation to prevent focal offset.
     */
    fun splitWordForOrp(rawWord: String): OrpSegments {
        val (clean, leading, trailing) = stripPunctuation(rawWord)
        
        if (clean.isEmpty()) return OrpSegments(leading + trailing, "", "")

        // Handle very long words via hyphenation to maintain focus
        if (clean.length > 13) {
            val head = clean.substring(0, 9) + "-"
            val tail = clean.substring(9) + trailing
            val idx = getOrpIndex(head)
            return OrpSegments(
                left = leading + head.substring(0, idx),
                orpChar = head[idx].toString(),
                right = head.substring(idx + 1),
                overflow = tail
            )
        }

        val idx = getOrpIndex(clean)
        val left = leading + clean.substring(0, idx)
        val orpChar = clean[idx].toString()
        val right = clean.substring(idx + 1) + trailing
        
        return OrpSegments(left, orpChar, right)
    }
}

data class OrpSegments(
    val left: String,
    val orpChar: String,
    val right: String,
    val overflow: String? = null
)
