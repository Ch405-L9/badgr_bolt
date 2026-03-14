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
