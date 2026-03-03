package com.badgr.orbreader.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.repository.ReadingSessionRepository
import com.badgr.orbreader.data.repository.StatsSnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ReadingSessionRepository(
        BookDatabase.getInstance(application).readingSessionDao()
    )

    private val _snapshot = MutableStateFlow(StatsSnapshot())
    val snapshot: StateFlow<StatsSnapshot> = _snapshot.asStateFlow()

    val sessions = repo.allSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        viewModelScope.launch { _snapshot.value = repo.getSnapshot() }
        // refresh when sessions table changes
        viewModelScope.launch {
            repo.allSessions.collect {
                _snapshot.value = repo.getSnapshot()
            }
        }
    }
}
