import os, sys

base = os.getcwd()

def write(rel, content):
    full = os.path.join(base, rel)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        f.write(content)
    print(f"  ✓  {rel}")

print("patch_2_4_0.py — BADGR Bolt Achievement Engine + Performance Tracker")
print("=" * 70)

# ─────────────────────────────────────────────────────────────────────────────
# 1. AchievementEntity.kt
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/AchievementEntity.kt", """
package com.badgr.orbreader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 2. AchievementDao.kt
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/AchievementDao.kt", """
package com.badgr.orbreader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlock(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY unlockedAt ASC")
    fun getAllUnlocked(): Flow<List<AchievementEntity>>

    @Query("SELECT id FROM achievements")
    suspend fun getUnlockedIds(): List<String>

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun unlockedCount(): Int
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 3. AchievementDefinitions.kt
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/achievements/AchievementDefinitions.kt", """
package com.badgr.orbreader.achievements

/**
 * BADGR Bolt — Achievement System
 *
 * BOLT RANK: A dynamic rank computed from the user's last 5 qualifying sessions
 * (min 500 words, min 2 min active). Ranks are based on effective WPM — actual
 * words delivered during active reading time, not configured speed. This is
 * unique to BADGR Bolt and not found in any other RSVP reader.
 *
 * ACHIEVEMENTS: 20 achievements across 5 categories — all free, no Pro gate.
 * Designed to reward genuine reading skill development, not trivial engagement.
 */

enum class AchievementTier { SINGLE, BRONZE, SILVER, GOLD }

data class AchievementDef(
    val id       : String,
    val title    : String,
    val description: String,
    val emoji    : String,
    val tier     : AchievementTier = AchievementTier.SINGLE,
    val category : String
)

/**
 * The BOLT RANK system — BADGR Bolt's signature differentiator.
 * Dynamic rank updated after every qualifying session. Shows users
 * exactly where their reading performance stands.
 */
enum class BoltRank(
    val label    : String,
    val subtitle : String,
    val emoji    : String,
    val minAvgWpm: Int,
    val colorHex : Long
) {
    SPARK   ("Spark",   "Just getting started",   "🌱", 0,   0xFF8892B0),
    BOLT    ("Bolt",    "Finding your pace",       "⚡", 150, 0xFF00CED1),
    FLASH   ("Flash",  "Reading with purpose",     "💨", 250, 0xFF4CAF50),
    STORM   ("Storm",   "High-velocity reader",    "🌩", 350, 0xFFFFC107),
    THUNDER ("Thunder", "Elite speed reader",      "🔱", 500, 0xFFE040FB);

    companion object {
        fun fromAvgWpm(wpm: Int): BoltRank =
            values().reversed().firstOrNull { wpm >= it.minAvgWpm } ?: SPARK
    }
}

object AchievementDefinitions {

    val ALL: List<AchievementDef> = listOf(

        // ── Onboarding ────────────────────────────────────────────────────
        AchievementDef(
            id = "first_bolt", title = "First Bolt",
            description = "Complete your first RSVP reading session",
            emoji = "⚡", category = "Onboarding"
        ),
        AchievementDef(
            id = "open_book", title = "Open Book",
            description = "Import your first book into BADGR Bolt",
            emoji = "📖", category = "Onboarding"
        ),

        // ── Speed Milestones ──────────────────────────────────────────────
        // Based on effective WPM — actual words per active reading minute.
        // Average adult reads ~238 WPM. 300+ is genuinely fast. 500+ is elite.
        AchievementDef(
            id = "wpm_200", title = "Centurion",
            description = "Average 200+ effective WPM in a qualifying session",
            emoji = "🎯", category = "Speed"
        ),
        AchievementDef(
            id = "wpm_300", title = "Triple Crown",
            description = "Average 300+ effective WPM in a qualifying session",
            emoji = "🔵", category = "Speed"
        ),
        AchievementDef(
            id = "wpm_400", title = "Quarter Miler",
            description = "Average 400+ effective WPM in a qualifying session",
            emoji = "🟡", category = "Speed"
        ),
        AchievementDef(
            id = "wpm_500", title = "Lightning Rod",
            description = "Average 500+ effective WPM in a qualifying session",
            emoji = "🟣", category = "Speed"
        ),
        AchievementDef(
            id = "wpm_600", title = "Speed of Light",
            description = "Average 600+ effective WPM in a qualifying session",
            emoji = "✨", category = "Speed"
        ),

        // ── Endurance ─────────────────────────────────────────────────────
        AchievementDef(
            id = "words_10k", title = "Warm Up",
            description = "Read 10,000 total words in RSVP mode",
            emoji = "🌱", category = "Endurance"
        ),
        AchievementDef(
            id = "words_100k", title = "Getting Serious",
            description = "Read 100,000 total words in RSVP mode",
            emoji = "🔥", category = "Endurance"
        ),
        AchievementDef(
            id = "words_1m", title = "Million Word Club",
            description = "Read 1,000,000 total words in RSVP mode",
            emoji = "💎", category = "Endurance"
        ),
        AchievementDef(
            id = "marathon", title = "Marathon",
            description = "Read 10,000+ words in a single session",
            emoji = "🏃", category = "Endurance"
        ),
        AchievementDef(
            id = "deep_focus", title = "Deep Focus",
            description = "25+ active reading minutes in one session",
            emoji = "🧠", category = "Endurance"
        ),

        // ── Consistency ───────────────────────────────────────────────────
        AchievementDef(
            id = "streak_3", title = "Three Peat",
            description = "Read on 3 consecutive days",
            emoji = "📅", category = "Consistency"
        ),
        AchievementDef(
            id = "streak_7", title = "Week Warrior",
            description = "Read on 7 consecutive days",
            emoji = "🗓", category = "Consistency"
        ),
        AchievementDef(
            id = "streak_14", title = "Fortnight",
            description = "Read on 14 consecutive days",
            emoji = "🏆", category = "Consistency"
        ),
        AchievementDef(
            id = "sessions_10", title = "Getting Into It",
            description = "Complete 10 qualifying reading sessions",
            emoji = "📚", category = "Consistency"
        ),
        AchievementDef(
            id = "sessions_50", title = "Committed",
            description = "Complete 50 qualifying reading sessions",
            emoji = "🌟", category = "Consistency"
        ),

        // ── Mastery ───────────────────────────────────────────────────────
        AchievementDef(
            id = "improving", title = "Leveling Up",
            description = "Recent avg WPM is 25% above your baseline",
            emoji = "📈", category = "Mastery"
        ),
        AchievementDef(
            id = "consistent", title = "Steady Hand",
            description = "Last 10 sessions within a tight WPM range",
            emoji = "🎯", category = "Mastery"
        ),

        // ── ORP-Specific ──────────────────────────────────────────────────
        // Unique to BADGR Bolt — rewards reading without regression,
        // which is the primary skill RSVP/ORP training develops.
        AchievementDef(
            id = "orp_pure", title = "ORP Pure",
            description = "Complete a 1,000+ word session with zero rewinds",
            emoji = "👁", category = "ORP Mastery"
        )
    )

    val byId: Map<String, AchievementDef> = ALL.associateBy { it.id }
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 4. AchievementsEngine.kt
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/achievements/AchievementsEngine.kt", """
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
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 5. ReadingSessionEntity.kt — add rewindCount
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/ReadingSessionEntity.kt", """
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
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 6. ReadingSessionDao.kt — add analytics queries
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/ReadingSessionDao.kt", """
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
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 7. BookDao.kt — add bookCount
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/BookDao.kt", """
package com.badgr.orbreader.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    suspend fun getAllBooks_suspend(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("UPDATE books SET currentWordIndex = :index WHERE id = :bookId")
    suspend fun updateProgress(bookId: String, index: Int)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun bookCount(): Int
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 8. BookDatabase.kt — v5 migration
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/local/BookDatabase.kt", """
package com.badgr.orbreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities  = [BookEntity::class, ReadingSessionEntity::class, AchievementEntity::class],
    version   = 5,
    exportSchema = true
)
abstract class BookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile private var INSTANCE: BookDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN currentWordIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN coverPath TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS reading_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, bookId TEXT NOT NULL, bookTitle TEXT NOT NULL, wordsRead INTEGER NOT NULL, durationSeconds INTEGER NOT NULL, avgWpm INTEGER NOT NULL, timestamp INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reading_sessions ADD COLUMN rewindCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS achievements (id TEXT NOT NULL, unlockedAt INTEGER NOT NULL, PRIMARY KEY(id))")
            }
        }

        fun getInstance(context: Context): BookDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BookDatabase::class.java,
                    "orbreader.db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                .also { INSTANCE = it }
            }
    }
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 9. ReadingSessionRepository.kt — achievement checking + streak + rank
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/data/repository/ReadingSessionRepository.kt", """
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
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 10. ReaderViewModel.kt — wire session recording + active time tracking
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/ui/reader/ReaderViewModel.kt", """
package com.badgr.orbreader.ui.reader

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.repository.BookRepository
import com.badgr.orbreader.data.repository.ReadingSessionRepository
import com.badgr.orbreader.sync.CloudSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class ReaderUiState(
    val words        : List<String> = emptyList(),
    val currentIndex : Int          = 0,
    val wpm          : Int          = 150,
    val isPlaying    : Boolean      = false,
    val isLoading    : Boolean      = true
) {
    val currentWord: String get() = words.getOrElse(currentIndex) { "" }
    val progress: Float     get() = if (words.isEmpty()) 0f
                                    else currentIndex.toFloat() / words.size
}

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db          = BookDatabase.getInstance(application)
    private val repo        = BookRepository(context = application, bookDao = db.bookDao())
    private val sessionRepo = ReadingSessionRepository(
        dao            = db.readingSessionDao(),
        achievementDao = db.achievementDao()
    )

    private val _state      = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val _bookTitle  = MutableStateFlow("")
    val bookTitle: StateFlow<String> = _bookTitle.asStateFlow()

    private val _showOrpColor = MutableStateFlow(true)
    val showOrpColor: StateFlow<Boolean> = _showOrpColor.asStateFlow()

    /** Achievement IDs newly unlocked at end of session — consumed by UI. */
    private val _newAchievements = MutableStateFlow<List<String>>(emptyList())
    val newAchievements: StateFlow<List<String>> = _newAchievements.asStateFlow()

    // ── Session tracking ──────────────────────────────────────────────────
    private var currentBookId     : String  = ""
    private var sessionStartIndex : Int     = -1
    private var sessionHasStarted : Boolean = false
    private var sessionActiveMs   : Long    = 0L
    private var lastPlayStartMs   : Long    = 0L
    private var sessionRewindCount: Int     = 0

    private var playJob: Job? = null

    fun loadBook(bookId: String) {
        currentBookId = bookId
        viewModelScope.launch {
            val entity     = db.bookDao().getBookById(bookId)
            val savedIndex = entity?.currentWordIndex ?: 0
            _bookTitle.value = entity?.title ?: ""
            val words = repo.loadWords(bookId)
            _state.update {
                it.copy(words = words, currentIndex = savedIndex, isLoading = false)
            }
        }
    }

    fun saveProgress() {
        if (currentBookId.isEmpty()) return
        val index = _state.value.currentIndex

        // Capture any remaining active time if still playing when closed
        if (_state.value.isPlaying && sessionHasStarted) {
            sessionActiveMs += System.currentTimeMillis() - lastPlayStartMs
        }

        val wordsRead       = if (sessionStartIndex >= 0)
            (index - sessionStartIndex).coerceAtLeast(0) else 0
        val activeSeconds   = (sessionActiveMs / 1000L).toInt()
        val effectiveWpm    = if (activeSeconds > 0)
            ((wordsRead * 60f) / activeSeconds).toInt() else 0

        viewModelScope.launch {
            db.bookDao().updateProgress(currentBookId, index)

            // Record session and check achievements if above threshold
            if (wordsRead >= 100 && activeSeconds >= 60) {
                try {
                    val bookCount  = db.bookDao().bookCount()
                    val newlyUnlocked = sessionRepo.recordSession(
                        bookId          = currentBookId,
                        bookTitle       = _bookTitle.value,
                        wordsRead       = wordsRead,
                        durationSeconds = activeSeconds,
                        wpm             = effectiveWpm,
                        rewindCount     = sessionRewindCount,
                        booksImported   = bookCount
                    )
                    if (newlyUnlocked.isNotEmpty()) {
                        _newAchievements.value = newlyUnlocked
                    }
                } catch (e: Exception) {
                    Log.w("ReaderViewModel", "Session record failed (non-fatal): ${e.localizedMessage}")
                }
            }

            // Reset session tracking for next read
            sessionStartIndex  = -1
            sessionHasStarted  = false
            sessionActiveMs    = 0L
            lastPlayStartMs    = 0L
            sessionRewindCount = 0

            try {
                CloudSyncManager.pushProgress(currentBookId, index)
            } catch (e: Exception) {
                Log.w("ReaderViewModel", "Progress sync failed (non-fatal): ${e.localizedMessage}")
            }
        }
    }

    /** Call from UI after showing achievement notification. */
    fun consumeAchievements() { _newAchievements.value = emptyList() }

    fun toggleOrpColor(enabled: Boolean) { _showOrpColor.value = enabled }

    fun togglePlayPause() {
        val playing = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = playing) }
        if (playing) {
            // Mark session start index on first play
            if (!sessionHasStarted) {
                sessionStartIndex = _state.value.currentIndex
                sessionHasStarted = true
            }
            lastPlayStartMs = System.currentTimeMillis()
            startPlayback()
        } else {
            // Accumulate active time on pause
            sessionActiveMs += System.currentTimeMillis() - lastPlayStartMs
            stopPlayback()
        }
    }

    fun adjustWpm(delta: Int) {
        val newWpm = (_state.value.wpm + delta).coerceIn(60, 1200)
        _state.update { it.copy(wpm = newWpm) }
        if (_state.value.isPlaying) { stopPlayback(); startPlayback() }
    }

    fun skipSeconds(seconds: Int) {
        // Count backward seeks for ORP Pure achievement tracking
        if (seconds < 0) sessionRewindCount++

        val wpm         = _state.value.wpm
        val wordsToSkip = (wpm * abs(seconds) / 60f).roundToInt().coerceAtLeast(1)
        val delta       = if (seconds > 0) wordsToSkip else -wordsToSkip
        val newIndex    = (_state.value.currentIndex + delta)
            .coerceIn(0, (_state.value.words.size - 1).coerceAtLeast(0))
        _state.update { it.copy(currentIndex = newIndex) }
    }

    fun seekTo(index: Int) {
        _state.update { it.copy(currentIndex = index.coerceIn(0, it.words.lastIndex)) }
    }

    private fun startPlayback() {
        playJob?.cancel()
        playJob = viewModelScope.launch {
            while (_state.value.isPlaying) {
                val s = _state.value
                if (s.currentIndex >= s.words.lastIndex) {
                    _state.update { it.copy(isPlaying = false) }
                    // Capture final active segment
                    sessionActiveMs += System.currentTimeMillis() - lastPlayStartMs
                    break
                }
                delay(60_000L / s.wpm)
                _state.update { it.copy(currentIndex = it.currentIndex + 1) }
            }
        }
    }

    private fun stopPlayback() { playJob?.cancel() }

    override fun onCleared() {
        saveProgress()
        stopPlayback()
    }
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 11. StatsViewModel.kt — expose achievements + rank
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/ui/stats/StatsViewModel.kt", """
package com.badgr.orbreader.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.AchievementEntity
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.repository.ReadingSessionRepository
import com.badgr.orbreader.data.repository.StatsSnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val db   = BookDatabase.getInstance(application)
    private val repo = ReadingSessionRepository(
        dao            = db.readingSessionDao(),
        achievementDao = db.achievementDao()
    )

    private val _snapshot = MutableStateFlow(StatsSnapshot())
    val snapshot: StateFlow<StatsSnapshot> = _snapshot.asStateFlow()

    val sessions: StateFlow<List<com.badgr.orbreader.data.local.ReadingSessionEntity>> =
        repo.allSessions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val unlockedAchievements: StateFlow<List<AchievementEntity>> =
        db.achievementDao().getAllUnlocked()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { _snapshot.value = repo.getSnapshot() }
        viewModelScope.launch {
            repo.allSessions.collect { _snapshot.value = repo.getSnapshot() }
        }
    }
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# 12. StatsScreen.kt — Bolt Rank card + achievement grid + existing stats
# ─────────────────────────────────────────────────────────────────────────────
write("app/src/main/java/com/badgr/orbreader/ui/stats/StatsScreen.kt", """
package com.badgr.orbreader.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.achievements.AchievementDef
import com.badgr.orbreader.achievements.AchievementDefinitions
import com.badgr.orbreader.achievements.BoltRank
import com.badgr.orbreader.data.local.ReadingSessionEntity
import com.badgr.orbreader.ui.theme.ReaderColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel = viewModel()) {
    val snapshot             by vm.snapshot.collectAsState()
    val sessions             by vm.sessions.collectAsState()
    val unlockedAchievements by vm.unlockedAchievements.collectAsState()

    val unlockedIds = remember(unlockedAchievements) {
        unlockedAchievements.map { it.id }.toSet()
    }
    val achievementRows = remember { AchievementDefinitions.ALL.chunked(4) }

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Reading Stats", color = ReaderColors.textWarm, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── BOLT RANK card — always visible, no gate ──────────────────
            item {
                Spacer(Modifier.height(4.dp))
                BoltRankCard(rank = snapshot.boltRank)
            }

            // ── Achievement grid header ───────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Achievements",
                        color = ReaderColors.textWarm,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${unlockedIds.size} / ${AchievementDefinitions.ALL.size}",
                        color = ReaderColors.orpFocal,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Achievement grid — 4 per row ──────────────────────────────
            items(achievementRows) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { def ->
                        AchievementChip(
                            def        = def,
                            isUnlocked = def.id in unlockedIds,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                    // Fill empty slots if row has fewer than 4
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            // ── Stats summary — only shown if sessions exist ──────────────
            if (snapshot.totalSessions > 0) {

                item {
                    Text(
                        "Performance",
                        color = ReaderColors.textWarm,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Best WPM",   "${snapshot.bestWpm}",    "wpm", Modifier.weight(1f))
                        StatCard("Avg WPM",    "${snapshot.averageWpm}", "wpm", Modifier.weight(1f))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Sessions",   "${snapshot.totalSessions}",    "",    Modifier.weight(1f))
                        StatCard("Total Time", "${snapshot.totalReadingMins}", "min", Modifier.weight(1f))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Streak",     "${snapshot.currentStreakDays}", "days", Modifier.weight(1f))
                        StatCard("Total Words", formatLargeNumber(snapshot.totalWordsRead), "words", Modifier.weight(1f))
                    }
                }

                // ── Recent sessions ───────────────────────────────────────
                item {
                    Text(
                        "Recent Sessions",
                        color = ReaderColors.textWarm,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                items(sessions.take(20)) { session -> SessionRow(session) }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BOLT RANK CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BoltRankCard(rank: BoltRank) {
    val rankColor = Color(rank.colorHex)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = rankColor.copy(alpha = 0.10f),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, rankColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "BOLT RANK",
                    fontFamily  = FontFamily.Monospace,
                    fontSize    = 9.sp,
                    fontWeight  = FontWeight.Bold,
                    color       = rankColor,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    rank.label,
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.Black,
                    color      = ReaderColors.textWarm
                )
                Text(
                    rank.subtitle,
                    fontSize = 12.sp,
                    color    = ReaderColors.textDimmed
                )
            }
            Text(rank.emoji, fontSize = 44.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACHIEVEMENT CHIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AchievementChip(def: AchievementDef, isUnlocked: Boolean, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color    = if (isUnlocked) ReaderColors.orpFocal.copy(alpha = 0.10f)
                   else ReaderColors.background,
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(
            1.dp,
            if (isUnlocked) ReaderColors.orpFocal.copy(alpha = 0.45f)
            else ReaderColors.guideLine
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                def.emoji,
                fontSize = 22.sp,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.25f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                def.title,
                fontSize  = 9.sp,
                color     = if (isUnlocked) ReaderColors.textWarm else ReaderColors.textDimmed,
                textAlign = TextAlign.Center,
                fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                maxLines  = 2,
                overflow  = TextOverflow.Ellipsis,
                modifier  = Modifier.alpha(if (isUnlocked) 1f else 0.45f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXISTING COMPONENTS (unchanged)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = ReaderColors.orpFocal.copy(alpha = 0.08f),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = ReaderColors.textDimmed, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = ReaderColors.textWarm, fontSize = 24.sp, fontWeight = FontWeight.Black)
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(unit, color = ReaderColors.orpFocal, fontSize = 12.sp,
                         modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: ReadingSessionEntity) {
    val dateStr = remember(session.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.timestamp))
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = ReaderColors.orpFocal.copy(alpha = 0.04f),
        shape    = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(session.bookTitle, color = ReaderColors.textWarm, fontSize = 13.sp,
                     fontWeight = FontWeight.Medium, maxLines = 1)
                Text(dateStr, color = ReaderColors.textDimmed, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${session.avgWpm} WPM", color = ReaderColors.orpFocal,
                     fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${session.wordsRead} words", color = ReaderColors.textDimmed, fontSize = 11.sp)
            }
        }
    }
}

private fun formatLargeNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000     -> "%.1fK".format(n / 1_000.0)
    else           -> n.toString()
}
""".lstrip())

# ─────────────────────────────────────────────────────────────────────────────
# CHANGELOG entry
# ─────────────────────────────────────────────────────────────────────────────
changelog_path = os.path.join(base, "CHANGELOG.md")
with open(changelog_path, "r") as f:
    existing = f.read()

entry = """## [2.4.0] — 2026-03-14
### Added
- AchievementEntity.kt: Room entity for persisting unlocked achievements
- AchievementDao.kt: DAO for achievement unlock and query operations
- AchievementDefinitions.kt: 20 achievement definitions across 5 categories
- BoltRank enum: SPARK / BOLT / FLASH / STORM / THUNDER — dynamic rank based on effective WPM
- AchievementsEngine.kt: Pure evaluation engine — takes stats snapshot + session context, returns newly unlocked IDs
- BookDatabase migration 4→5: adds rewindCount to reading_sessions, creates achievements table
- ReadingSessionRepository: streak computation, baseline vs recent WPM improvement, consistency check, Bolt Rank, achievement checking on recordSession
- ReaderViewModel: active reading time tracking (excludes pauses), rewind counter, session recording on saveProgress, newAchievements StateFlow
- StatsViewModel: exposes unlockedAchievements StateFlow from AchievementDao
- StatsScreen: Bolt Rank card, 4-column achievement grid (locked/unlocked states), streak card
### Changed
- ReadingSessionEntity: added rewindCount field (default 0)
- ReadingSessionDao: added getFirstFive/LastFive/LastTen/RankSessions and getQualifyingDays queries
- BookDao: added bookCount() query
- BookDatabase: version 4→5, achievementDao() abstract method added
- StatsScreen: ProGate removed — stats and achievements visible to all users
### Known Issues
- TD-004: Deprecated statusBarColor in Theme.kt (deferred to 2.5.x)
- TD-006: No unit or instrumentation tests
- TD-007: Email verification not enforced for app access
### Next Milestone
- 2.4.1: Wire achievement unlock notification in ReaderScreen (Snackbar on session end)

"""

with open(changelog_path, "w") as f:
    f.write(entry + existing)
print("  ✓  CHANGELOG.md")

print()
print("=" * 70)
print("patch_2_4_0.py complete — 12 files written.")
print()
print("Next steps:")
print("  1.  ./gradlew assembleDebug")
print("  2.  Fix any compile errors (report output here)")
print("  3.  Install on SM-S721U and do a qualifying read session")
print("  4.  Verify StatsScreen shows Bolt Rank card and achievement grid")
