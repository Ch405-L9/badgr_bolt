package com.badgr.orbreader.billing

/**
 * Single source of truth for feature entitlement.
 *
 * PRIVATE_ROLLOUT_ALL_OPEN = true   → all Pro features unlocked (testing / private rollout)
 * PRIVATE_ROLLOUT_ALL_OPEN = false  → entitlement enforced via [isProUnlocked]
 *
 * When billing is implemented, set PRIVATE_ROLLOUT_ALL_OPEN = false and wire
 * [isProUnlocked] to the result of InAppPurchaseManager's entitlement check.
 * When Firebase Auth is gating cloud sync specifically, wire [cloudSync] to
 * also require a non-null Firebase user in addition to Pro entitlement.
 */
object ProGate {

    // ── Toggle this to false when billing goes live ───────────────────────────
    private const val PRIVATE_ROLLOUT_ALL_OPEN = true

    // ── Free tier limits ──────────────────────────────────────────────────────
    const val FREE_BOOK_LIMIT = 5

    // ── Entitlement source ────────────────────────────────────────────────────
    // Replace this stub with InAppPurchaseManager.hasProEntitlement() once billing is wired.
    private var isProUnlocked: Boolean = false

    /**
     * Master Pro check. All feature gates read from here.
     * During private rollout this always returns true.
     */
    val isPro: Boolean
        get() = PRIVATE_ROLLOUT_ALL_OPEN || isProUnlocked

    // ── Feature gates ─────────────────────────────────────────────────────────
    val statsScreen:   Boolean get() = isPro
    val cloudSync:     Boolean get() = isPro
    val unlimitedLib:  Boolean get() = isPro
    val customThemes:  Boolean get() = isPro
    val tts:           Boolean get() = isPro

    // ── Called by InAppPurchaseManager once billing is implemented ────────────
    fun setProEntitlement(unlocked: Boolean) {
        isProUnlocked = unlocked
    }

    // ── Called on sign-out or purchase revocation ─────────────────────────────
    fun revokeEntitlement() {
        isProUnlocked = false
    }
}
