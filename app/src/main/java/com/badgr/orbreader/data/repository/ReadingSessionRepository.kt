package com.badgr.orbreader.data.repository

import com.badgr.orbreader.data.local.ReadingSessionDao
import com.badgr.orbreader.data.local.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

data class StatsSnapshot(
    val totalSessions     : Int    = 0,
    val totalWordsRead    : Long   = 0L,
    val totalReadingMins  : Int    = 0,
    val bestWpm           : Int    = 0,
    val averageWpm        : Int    = 0,
    val recentSessions    : List<ReadingSessionEntity> = emptyList()
)

class ReadingSessionRepository(private val dao: ReadingSessionDao) {

    val allSessions: Flow<List<ReadingSessionEntity>> = dao.getAllSessions()

    suspend fun recordSession(
        bookId          : String,
        bookTitle       : String,
        wordsRead       : Int,
        durationSeconds : Int,
        wpm             : Int
    ) {
        if (wordsRead < 5) return   // discard trivial sessions
        dao.insertSession(
            ReadingSessionEntity(
                bookId          = bookId,
                bookTitle       = bookTitle,
                wordsRead       = wordsRead,
                durationSeconds = durationSeconds,
                avgWpm          = wpm
            )
        )
    }

    suspend fun getSnapshot(): StatsSnapshot {
        val recent = dao.getRecentSessions(10)
        return StatsSnapshot(
            totalSessions    = dao.totalSessions(),
            totalWordsRead   = dao.totalWordsRead() ?: 0L,
            totalReadingMins = ((dao.totalReadingSeconds() ?: 0L) / 60L).toInt(),
            bestWpm          = dao.bestWpm() ?: 0,
            averageWpm       = (dao.averageWpm() ?: 0.0).toInt(),
            recentSessions   = recent
        )
    }
}
