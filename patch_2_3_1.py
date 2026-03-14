
import os

BASE = os.path.expanduser("~/AndroidStudioProjects/badgr_bolt/app/src/main/java/com/badgr/orbreader")

# ── 1. ProGate.kt ─────────────────────────────────────────────────────────────
progate = """\
package com.badgr.orbreader.billing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for feature entitlement.
 *
 * PRIVATE_ROLLOUT_ALL_OPEN = true   → all Pro features unlocked (testing / private rollout)
 * PRIVATE_ROLLOUT_ALL_OPEN = false  → entitlement enforced via [isPro]
 *
 * OrbReaderApp collects InAppPurchaseManager.isPro StateFlow and calls
 * setProEntitlement() to keep this gate in sync — closes TD-003.
 */
object ProGate {

    // ── Toggle this to false when billing goes live ───────────────────────────
    private const val PRIVATE_ROLLOUT_ALL_OPEN = true

    // ── Free tier limits ──────────────────────────────────────────────────────
    const val FREE_BOOK_LIMIT = 5

    // ── Reactive entitlement state ────────────────────────────────────────────
    private val _isPro = MutableStateFlow(PRIVATE_ROLLOUT_ALL_OPEN)

    /** Observable stream — collect in UI / ViewModel layers. */
    val isProFlow: StateFlow<Boolean> = _isPro.asStateFlow()

    /** Synchronous accessor — use in non-Compose, non-suspend contexts. */
    val isPro: Boolean get() = _isPro.value

    // ── Feature gates ─────────────────────────────────────────────────────────
    val statsScreen:  Boolean get() = isPro
    val cloudSync:    Boolean get() = isPro
    val unlimitedLib: Boolean get() = isPro
    val customThemes: Boolean get() = isPro
    val tts:          Boolean get() = isPro

    // ── Called by OrbReaderApp collector once billing is wired ────────────────
    fun setProEntitlement(unlocked: Boolean) {
        _isPro.value = PRIVATE_ROLLOUT_ALL_OPEN || unlocked
    }

    // ── Called on sign-out or purchase revocation ─────────────────────────────
    fun revokeEntitlement() {
        _isPro.value = PRIVATE_ROLLOUT_ALL_OPEN  // respects rollout flag
    }
}
"""

progate_path = os.path.join(BASE, "billing", "ProGate.kt")
with open(progate_path, "w") as f:
    f.write(progate)
print(f"Written: {progate_path}")

# ── 2. OrbReaderApp.kt ────────────────────────────────────────────────────────
orbapp = """\
package com.badgr.orbreader

import android.app.Application
import com.badgr.orbreader.billing.InAppPurchaseManager
import com.badgr.orbreader.billing.ProGate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Application subclass – declared in AndroidManifest.xml via android:name=".OrbReaderApp".
 * Initialises global singletons: billing client.
 * Wires ProGate to observe InAppPurchaseManager.isPro StateFlow — closes TD-003.
 */
class OrbReaderApp : Application() {

    lateinit var purchaseManager: InAppPurchaseManager
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        purchaseManager = InAppPurchaseManager(
            context = this,
            onPurchaseSuccess = {
                // Entitlement state is driven by the isPro StateFlow collector below
            },
            onPurchaseError = { error ->
                android.util.Log.e("InAppPurchase", "Purchase error: \$error")
            }
        )
        purchaseManager.connect()

        // TD-003: Wire ProGate to observe InAppPurchaseManager.isPro StateFlow
        applicationScope.launch {
            purchaseManager.isPro.collect { isPro ->
                ProGate.setProEntitlement(isPro)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        purchaseManager.disconnect()
        applicationScope.cancel()
    }
}
"""

orbapp_path = os.path.join(BASE, "OrbReaderApp.kt")
with open(orbapp_path, "w") as f:
    f.write(orbapp)
print(f"Written: {orbapp_path}")
print("Patch 2.3.1 complete.")
