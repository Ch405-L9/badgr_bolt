package com.badgr.orbreader.ui.reader

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.preferences.UserPreferencesRepository
import com.badgr.orbreader.data.repository.BookRepository
import com.badgr.orbreader.data.repository.ReadingSessionRepository
import com.badgr.orbreader.sync.CloudSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
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
    private val prefsRepo   = UserPreferencesRepository(application)

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val _bookTitle = MutableStateFlow("")
    val bookTitle: StateFlow<String> = _bookTitle.asStateFlow()

    /** Driven by DataStore — reflects user's saved preference in real time. */
    val showOrpColor: StateFlow<Boolean> = prefsRepo.preferences
        .map { it.showOrpColor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    /** 0=cyan 1=green 2=amber 3=purple 4=red — saved in Settings, shown here. */
    val orpColorIndex: StateFlow<Int> = prefsRepo.preferences
        .map { it.orpColorIndex }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Font family index — 0=Mono, 1=JetBrains, 2=Literata, 3=Merriweather, 4=Atkinson, 5=OpenSans */
    val fontIndex: StateFlow<Int> = prefsRepo.preferences
        .map { it.fontIndex }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Font size in sp — saved in Settings, applied in reader. */
    val fontSize: StateFlow<Int> = prefsRepo.preferences
        .map { it.fontSize }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 40)

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
            val words      = repo.loadWords(bookId)
            // Load saved WPM from DataStore so Settings value is respected
            val savedWpm   = prefsRepo.preferences.first().defaultWpm
            _state.update {
                it.copy(words = words, currentIndex = savedIndex, isLoading = false, wpm = savedWpm)
            }
        }
    }

    fun saveProgress() {
        if (currentBookId.isEmpty()) return
        val index = _state.value.currentIndex

        if (_state.value.isPlaying && sessionHasStarted) {
            sessionActiveMs += System.currentTimeMillis() - lastPlayStartMs
        }

        val wordsRead     = if (sessionStartIndex >= 0)
            (index - sessionStartIndex).coerceAtLeast(0) else 0
        val activeSeconds = (sessionActiveMs / 1000L).toInt()
        val effectiveWpm  = if (activeSeconds > 0)
            ((wordsRead * 60f) / activeSeconds).toInt() else 0

        viewModelScope.launch {
            db.bookDao().updateProgress(currentBookId, index)

            if (wordsRead >= 100 && activeSeconds >= 60) {
                try {
                    val bookCount     = db.bookDao().bookCount()
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
                    Log.w("ReaderViewModel", "Session record failed: ${e.localizedMessage}")
                }
            }

            sessionStartIndex  = -1
            sessionHasStarted  = false
            sessionActiveMs    = 0L
            lastPlayStartMs    = 0L
            sessionRewindCount = 0

            try {
                CloudSyncManager.pushProgress(currentBookId, index)
            } catch (e: Exception) {
                Log.w("ReaderViewModel", "Progress sync failed: ${e.localizedMessage}")
            }
        }
    }

    fun consumeAchievements() { _newAchievements.value = emptyList() }

    fun togglePlayPause() {
        val playing = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = playing) }
        if (playing) {
            if (!sessionHasStarted) {
                sessionStartIndex = _state.value.currentIndex
                sessionHasStarted = true
            }
            lastPlayStartMs = System.currentTimeMillis()
            startPlayback()
        } else {
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
