package com.badgr.orbreader.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSessionEntity)

    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ReadingSessionEntity>>

    @Query("SELECT * FROM reading_sessions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 10): List<ReadingSessionEntity>

    @Query("SELECT COUNT(*) FROM reading_sessions")
    suspend fun totalSessions(): Int

    @Query("SELECT SUM(wordsRead) FROM reading_sessions")
    suspend fun totalWordsRead(): Long?

    @Query("SELECT MAX(avgWpm) FROM reading_sessions")
    suspend fun bestWpm(): Int?

    @Query("SELECT AVG(avgWpm) FROM reading_sessions")
    suspend fun averageWpm(): Double?

    @Query("SELECT SUM(durationSeconds) FROM reading_sessions")
    suspend fun totalReadingSeconds(): Long?

    // ── Baseline vs recent improvement tracking ───────────────────────────
    @Query("SELECT * FROM reading_sessions WHERE wordsRead >= 100 AND durationSeconds >= 60 ORDER BY timestamp ASC LIMIT 5")
    suspend fun getFirstFiveQualifyingSessions(): List<ReadingSessionEntity>

    @Query("SELECT * FROM reading_sessions WHERE wordsRead >= 100 AND durationSeconds >= 60 ORDER BY timestamp DESC LIMIT 5")
    suspend fun getLastFiveQualifyingSessions(): List<ReadingSessionEntity>

    // ── Consistency tracking (last 10 qualifying sessions) ────────────────
    @Query("SELECT * FROM reading_sessions WHERE wordsRead >= 100 AND durationSeconds >= 60 ORDER BY timestamp DESC LIMIT 10")
    suspend fun getLastTenQualifyingSessions(): List<ReadingSessionEntity>

    // ── Bolt Rank (last 5 sessions with min 500 words, 2 min active) ──────
    @Query("SELECT * FROM reading_sessions WHERE wordsRead >= 500 AND durationSeconds >= 120 ORDER BY timestamp DESC LIMIT 5")
    suspend fun getLastFiveRankSessions(): List<ReadingSessionEntity>

    // ── Streak — distinct calendar days with qualifying sessions, DESC ─────
    @Query("SELECT DISTINCT (timestamp / 86400000) FROM reading_sessions WHERE wordsRead >= 100 AND durationSeconds >= 60 ORDER BY (timestamp / 86400000) DESC")
    suspend fun getQualifyingDays(): List<Long>

    @Query("DELETE FROM reading_sessions")
    suspend fun clearAll()
}
