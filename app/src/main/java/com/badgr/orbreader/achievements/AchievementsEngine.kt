package com.badgr.orbreader.achievements

import com.badgr.orbreader.data.repository.StatsSnapshot

/**
 * Pure evaluation engine — no side effects, no persistence.
 * Given current stats and session context, returns IDs of achievements
 * that should be newly unlocked (not yet in alreadyUnlocked set).
 */
object AchievementsEngine {

    fun evaluate(
        snapshot              : StatsSnapshot,
        alreadyUnlocked       : Set<String>,
        booksImported         : Int,
        currentStreakDays      : Int,
        rewindCountThisSession: Int,
        activeMinutesThisSession: Int,
        wordsThisSession      : Int
    ): List<String> {

        val newly = mutableListOf<String>()

        fun check(id: String, condition: Boolean) {
            if (condition && id !in alreadyUnlocked) newly.add(id)
        }

        // Onboarding
        check("first_bolt",   snapshot.totalSessions >= 1)
        check("open_book",    booksImported >= 1)

        // Speed — bestWpm is the peak effective WPM across all sessions
        check("wpm_200",      snapshot.bestWpm >= 200)
        check("wpm_300",      snapshot.bestWpm >= 300)
        check("wpm_400",      snapshot.bestWpm >= 400)
        check("wpm_500",      snapshot.bestWpm >= 500)
        check("wpm_600",      snapshot.bestWpm >= 600)

        // Endurance — cumulative totals
        check("words_10k",    snapshot.totalWordsRead >= 10_000L)
        check("words_100k",   snapshot.totalWordsRead >= 100_000L)
        check("words_1m",     snapshot.totalWordsRead >= 1_000_000L)

        // Session endurance
        check("marathon",     wordsThisSession >= 10_000)
        check("deep_focus",   activeMinutesThisSession >= 25)

        // Consistency
        check("streak_3",     currentStreakDays >= 3)
        check("streak_7",     currentStreakDays >= 7)
        check("streak_14",    currentStreakDays >= 14)
        check("sessions_10",  snapshot.totalSessions >= 10)
        check("sessions_50",  snapshot.totalSessions >= 50)

        // Mastery — improvement over baseline
        if (snapshot.baselineAvgWpm > 0 && snapshot.recentAvgWpm > 0) {
            check("improving",
                snapshot.recentAvgWpm >= (snapshot.baselineAvgWpm * 1.25).toInt()
            )
        }

        // Steady Hand — last 10 sessions within tight range
        check("consistent", snapshot.isConsistent)

        // ORP Pure — no rewinds, minimum 1000 words
        check("orp_pure",
            rewindCountThisSession == 0 && wordsThisSession >= 1_000
        )

        return newly
    }
}
