package com.badgr.orbreader.data.repository

import com.badgr.orbreader.achievements.AchievementsEngine
import com.badgr.orbreader.achievements.BoltRank
import com.badgr.orbreader.data.local.AchievementDao
import com.badgr.orbreader.data.local.AchievementEntity
import com.badgr.orbreader.data.local.ReadingSessionDao
import com.badgr.orbreader.data.local.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

data class StatsSnapshot(
    val totalSessions    : Int     = 0,
    val totalWordsRead   : Long    = 0L,
    val totalReadingMins : Int     = 0,
    val bestWpm          : Int     = 0,
    val averageWpm       : Int     = 0,
    val recentSessions   : List<ReadingSessionEntity> = emptyList(),
    // Improvement tracking
    val baselineAvgWpm   : Int     = 0,
    val recentAvgWpm     : Int     = 0,
    // Consistency tracking
    val isConsistent     : Boolean = false,
    // Habit tracking
    val currentStreakDays : Int     = 0,
    // BOLT RANK — the app's signature differentiator
    val boltRank         : BoltRank = BoltRank.SPARK
)

class ReadingSessionRepository(
    private val dao           : ReadingSessionDao,
    private val achievementDao: AchievementDao
) {

    val allSessions: Flow<List<ReadingSessionEntity>> = dao.getAllSessions()

    /**
     * Record a qualifying session and evaluate achievements.
     * Returns the list of newly unlocked achievement IDs (may be empty).
     * Minimum threshold: 100 words AND 60 seconds active time.
     */
    suspend fun recordSession(
        bookId          : String,
        bookTitle       : String,
        wordsRead       : Int,
        durationSeconds : Int,
        wpm             : Int,
        rewindCount     : Int = 0,
        booksImported   : Int = 0
    ): List<String> {
        if (wordsRead < 100 || durationSeconds < 60) return emptyList()

        dao.insertSession(
            ReadingSessionEntity(
                bookId          = bookId,
                bookTitle       = bookTitle,
                wordsRead       = wordsRead,
                durationSeconds = durationSeconds,
                avgWpm          = wpm,
                rewindCount     = rewindCount
            )
        )

        val snapshot        = getSnapshot()
        val alreadyUnlocked = achievementDao.getUnlockedIds().toSet()

        val newly = AchievementsEngine.evaluate(
            snapshot                 = snapshot,
            alreadyUnlocked          = alreadyUnlocked,
            booksImported            = booksImported,
            currentStreakDays        = snapshot.currentStreakDays,
            rewindCountThisSession   = rewindCount,
            activeMinutesThisSession = durationSeconds / 60,
            wordsThisSession         = wordsRead
        )

        newly.forEach { id ->
            achievementDao.unlock(AchievementEntity(id = id))
        }

        return newly
    }

    suspend fun getSnapshot(): StatsSnapshot {
        val recent       = dao.getRecentSessions(10)
        val firstFive    = dao.getFirstFiveQualifyingSessions()
        val lastFive     = dao.getLastFiveQualifyingSessions()
        val lastTen      = dao.getLastTenQualifyingSessions()
        val rankSessions = dao.getLastFiveRankSessions()
        val streakDays   = computeStreak()
        val rankWpm      = if (rankSessions.isNotEmpty())
            rankSessions.map { it.avgWpm }.average().toInt()
        else 0

        return StatsSnapshot(
            totalSessions    = dao.totalSessions(),
            totalWordsRead   = dao.totalWordsRead() ?: 0L,
            totalReadingMins = ((dao.totalReadingSeconds() ?: 0L) / 60L).toInt(),
            bestWpm          = dao.bestWpm() ?: 0,
            averageWpm       = (dao.averageWpm() ?: 0.0).toInt(),
            recentSessions   = recent,
            baselineAvgWpm   = if (firstFive.isNotEmpty())
                firstFive.map { it.avgWpm }.average().toInt() else 0,
            recentAvgWpm     = if (lastFive.isNotEmpty())
                lastFive.map { it.avgWpm }.average().toInt() else 0,
            isConsistent     = computeIsConsistent(lastTen),
            currentStreakDays = streakDays,
            boltRank         = BoltRank.fromAvgWpm(rankWpm)
        )
    }

    private suspend fun computeStreak(): Int {
        val days = dao.getQualifyingDays()
        if (days.isEmpty()) return 0
        val todayDay = System.currentTimeMillis() / 86400000L
        // Only count streak if user read today or yesterday
        if (days[0] < todayDay - 1) return 0
        var streak = 1
        for (i in 0 until days.size - 1) {
            if (days[i] - days[i + 1] == 1L) streak++
            else break
        }
        return streak
    }

    private fun computeIsConsistent(sessions: List<ReadingSessionEntity>): Boolean {
        if (sessions.size < 10) return false
        val wpms = sessions.map { it.avgWpm.toDouble() }
        val avg  = wpms.average()
        if (avg == 0.0) return false
        val range = (wpms.max() - wpms.min())
        return range / avg < 0.40
    }
}
