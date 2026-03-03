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

    @Query("DELETE FROM reading_sessions")
    suspend fun clearAll()
}
