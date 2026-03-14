import os

BASE = os.path.expanduser("~/AndroidStudioProjects/badgr_bolt/app/src/main/java/com/badgr/orbreader")

# ── 1. UserPreferencesRepository.kt ──────────────────────────────────────────
prefs = """\
package com.badgr.orbreader.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "badgr_user_prefs")

data class UserPreferences(
    val defaultWpm   : Int     = 150,
    val fontSize     : Int     = 46,
    val showOrpColor : Boolean = true,
    val orpColorIndex: Int     = 0,     // 0=cyan, 1=pink, 2=green, 3=amber
    val isPro        : Boolean = false  // persisted Pro entitlement
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_WPM    = intPreferencesKey("default_wpm")
        val FONT_SIZE      = intPreferencesKey("font_size")
        val SHOW_ORP_COLOR = booleanPreferencesKey("show_orp_color")
        val ORP_COLOR_IDX  = intPreferencesKey("orp_color_index")
        val IS_PRO         = booleanPreferencesKey("is_pro")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences())
            else throw e
        }
        .map { prefs ->
            UserPreferences(
                defaultWpm    = prefs[Keys.DEFAULT_WPM]    ?: 150,
                fontSize      = prefs[Keys.FONT_SIZE]      ?: 46,
                showOrpColor  = prefs[Keys.SHOW_ORP_COLOR] ?: true,
                orpColorIndex = prefs[Keys.ORP_COLOR_IDX]  ?: 0,
                isPro         = prefs[Keys.IS_PRO]         ?: false
            )
        }

    suspend fun setDefaultWpm(wpm: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_WPM] = wpm.coerceIn(60, 1200) }
    }

    suspend fun setFontSize(size: Int) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size.coerceIn(28, 72) }
    }

    suspend fun setShowOrpColor(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_ORP_COLOR] = show }
    }

    suspend fun setOrpColorIndex(index: Int) {
        context.dataStore.edit { it[Keys.ORP_COLOR_IDX] = index.coerceIn(0, 3) }
    }

    suspend fun setIsPro(unlocked: Boolean) {
        context.dataStore.edit { it[Keys.IS_PRO] = unlocked }
    }
}
"""

prefs_path = os.path.join(BASE, "data/preferences/UserPreferencesRepository.kt")
with open(prefs_path, "w") as f:
    f.write(prefs)
print(f"Written: {prefs_path}")

# ── 2. OrbReaderApp.kt ────────────────────────────────────────────────────────
orbapp = """\
package com.badgr.orbreader

import android.app.Application
import com.badgr.orbreader.billing.InAppPurchaseManager
import com.badgr.orbreader.billing.ProGate
import com.badgr.orbreader.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application subclass – declared in AndroidManifest.xml via android:name=".OrbReaderApp".
 *
 * Responsibilities:
 *  - Initialise InAppPurchaseManager singleton
 *  - Restore persisted Pro entitlement to ProGate before billing reconnects (2.3.3)
 *  - Collect purchaseManager.isPro and keep ProGate + DataStore in sync (2.3.1 / 2.3.3)
 */
class OrbReaderApp : Application() {

    lateinit var purchaseManager: InAppPurchaseManager
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        userPreferencesRepository = UserPreferencesRepository(this)

        purchaseManager = InAppPurchaseManager(
            context          = this,
            onPurchaseSuccess = {
                // Entitlement state driven by isPro StateFlow collector below
            },
            onPurchaseError  = { error ->
                android.util.Log.e("InAppPurchase", "Purchase error: $error")
            }
        )

        // 2.3.3: Restore persisted entitlement immediately so ProGate is correct
        // before the billing client finishes its async reconnection.
        applicationScope.launch(Dispatchers.IO) {
            val persisted = userPreferencesRepository.preferences.first().isPro
            ProGate.setProEntitlement(persisted)
        }

        purchaseManager.connect()

        // 2.3.1 + 2.3.3: Observe live entitlement — update ProGate and persist on every change.
        applicationScope.launch {
            purchaseManager.isPro.collect { isPro ->
                ProGate.setProEntitlement(isPro)
                applicationScope.launch(Dispatchers.IO) {
                    userPreferencesRepository.setIsPro(isPro)
                }
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
print("Patch 2.3.3 complete.")
