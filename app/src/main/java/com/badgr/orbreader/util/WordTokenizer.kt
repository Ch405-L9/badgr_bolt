package com.badgr.orbreader.util

/**
 * Splits plain text into a list of non-blank tokens.
 * Splits on any whitespace; filters empty strings.
 *
 * This is intentionally trivial – swap the regex for something smarter
 * (e.g. sentence-aware, punctuation-preserving) without changing callers.
 */
object WordTokenizer {

    fun tokenize(text: String): List<String> =
        text.split(Regex("\\s+")).filter { it.isNotBlank() }
}
