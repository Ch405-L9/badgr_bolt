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
