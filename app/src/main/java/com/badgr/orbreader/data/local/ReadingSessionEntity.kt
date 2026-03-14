package com.badgr.orbreader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_sessions")
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id      : Long = 0,
    val bookId          : String,
    val bookTitle       : String,
    val wordsRead       : Int,
    val durationSeconds : Int,
    val avgWpm          : Int,
    val rewindCount     : Int  = 0,
    val timestamp       : Long = System.currentTimeMillis()
)
