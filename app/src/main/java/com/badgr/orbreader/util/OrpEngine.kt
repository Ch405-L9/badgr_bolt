package com.badgr.orbreader.util

object OrpEngine {

    fun getOrpIndex(word: String): Int {
        val len = word.trim().length
        return when {
            len <= 2  -> 0
            len <= 5  -> 1
            len <= 9  -> 2
            len <= 13 -> 3
            else      -> 4
        }
    }

    fun stripPunctuation(word: String): Triple<String, String, String> {
        val leading  = word.takeWhile  { !it.isLetterOrDigit() }
        val trailing = word.takeLastWhile { !it.isLetterOrDigit() }
        val clean    = word.removePrefix(leading).removeSuffix(trailing)
        return Triple(clean, leading, trailing)
    }

    fun splitWordForOrp(rawWord: String): OrpSegments {
        val (clean, leading, trailing) = stripPunctuation(rawWord)
        if (clean.isEmpty()) return OrpSegments(leading + trailing, "", "")

        if (clean.length > 13) {
            val firstHalf  = clean.substring(0, 10) + "-"
            val secondHalf = clean.substring(10) + trailing
            val idx = getOrpIndex(firstHalf.dropLast(1))
            val left    = if (idx > 0) leading + firstHalf.substring(0, idx) else leading
            val orpChar = firstHalf[idx].toString()
            val right   = firstHalf.substring(idx + 1)
            return OrpSegments(left, orpChar, right, overflow = secondHalf)
        }

        val idx     = getOrpIndex(clean)
        val left    = if (idx > 0) leading + clean.substring(0, idx) else leading
        val orpChar = clean[idx].toString()
        val right   = if (idx + 1 < clean.length) clean.substring(idx + 1) + trailing else trailing
        return OrpSegments(left, orpChar, right)
    }
}

data class OrpSegments(
    val left:     String,
    val orpChar:  String,
    val right:    String,
    val overflow: String? = null
)
