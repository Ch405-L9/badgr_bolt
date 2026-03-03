package com.badgr.orbreader.billing

/**
 * Single source of truth for feature entitlement.
 *
 * Private rollout: isPro = true unlocks everything.
 * When Google Play Billing is wired in, replace this with a
 * real entitlement check from InAppPurchaseManager.
 *
 * Feature flags:
 *   STATS_SCREEN    - reading performance analytics
 *   CLOUD_SYNC      - Firebase library/progress sync
 *   UNLIMITED_LIB   - no book count cap
 *   CUSTOM_THEMES   - color palette beyond default
 *   TTS             - text-to-speech
 */
object ProGate {

    // ── Flip to false to simulate Free tier behavior ───────────────────────
    private const val PRIVATE_ROLLOUT_ALL_OPEN = true

    val isPro: Boolean get() = PRIVATE_ROLLOUT_ALL_OPEN

    // Individual feature gates — override individually if needed
    val statsScreen   : Boolean get() = isPro
    val cloudSync     : Boolean get() = isPro
    val unlimitedLib  : Boolean get() = isPro
    val customThemes  : Boolean get() = isPro
    val tts           : Boolean get() = isPro

    // Free tier hard limits (enforced when !isPro)
    const val FREE_BOOK_LIMIT = 5
}
