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

const val THEME_SYSTEM = 0
const val THEME_LIGHT  = 1
const val THEME_DARK   = 2

data class UserPreferences(
    val defaultWpm   : Int     = 150,
    val fontSize     : Int     = 46,
    val showOrpColor : Boolean = true,
    val orpColorIndex: Int     = 0,
    val isPro        : Boolean = false,
    val themeMode    : Int     = THEME_SYSTEM,
    val fontIndex    : Int     = 0,
    val chunkSize    : Int     = 1
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val DEFAULT_WPM    = intPreferencesKey("default_wpm")
        val FONT_SIZE      = intPreferencesKey("font_size")
        val SHOW_ORP_COLOR = booleanPreferencesKey("show_orp_color")
        val ORP_COLOR_IDX  = intPreferencesKey("orp_color_index")
        val IS_PRO         = booleanPreferencesKey("is_pro")
        val THEME_MODE     = intPreferencesKey("theme_mode")
        val FONT_INDEX     = intPreferencesKey("font_index")
        val CHUNK_SIZE     = intPreferencesKey("chunk_size")
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
                isPro         = prefs[Keys.IS_PRO]         ?: false,
                themeMode     = prefs[Keys.THEME_MODE]     ?: THEME_SYSTEM,
                fontIndex     = prefs[Keys.FONT_INDEX]     ?: 0,
                chunkSize     = prefs[Keys.CHUNK_SIZE]     ?: 1
            )
        }

    suspend fun setDefaultWpm(wpm: Int)       { context.dataStore.edit { it[Keys.DEFAULT_WPM]    = wpm.coerceIn(60, 1200) } }
    suspend fun setFontSize(size: Int)         { context.dataStore.edit { it[Keys.FONT_SIZE]      = size.coerceIn(28, 72) } }
    suspend fun setShowOrpColor(show: Boolean) { context.dataStore.edit { it[Keys.SHOW_ORP_COLOR] = show } }
    suspend fun setOrpColorIndex(index: Int)   { context.dataStore.edit { it[Keys.ORP_COLOR_IDX]  = index.coerceIn(0, 4) } }
    suspend fun setIsPro(unlocked: Boolean)    { context.dataStore.edit { it[Keys.IS_PRO]         = unlocked } }
    suspend fun setThemeMode(mode: Int)        { context.dataStore.edit { it[Keys.THEME_MODE]     = mode.coerceIn(0, 2) } }
    suspend fun setFontIndex(index: Int)       { context.dataStore.edit { it[Keys.FONT_INDEX]     = index.coerceIn(0, 5) } }
    suspend fun setChunkSize(size: Int)        { context.dataStore.edit { it[Keys.CHUNK_SIZE]     = size.coerceIn(1, 4) } }
}
