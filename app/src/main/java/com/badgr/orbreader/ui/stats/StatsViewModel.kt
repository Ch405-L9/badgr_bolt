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
