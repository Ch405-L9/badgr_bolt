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
    private val prefsRepo = UserPreferencesRepository(application)

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val _bookTitle = MutableStateFlow("")
    val bookTitle: StateFlow<String> = _bookTitle.asStateFlow()

    val showOrpColor: StateFlow<Boolean> = prefsRepo.preferences
        .map { it.showOrpColor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val orpColorIndex: StateFlow<Int> = prefsRepo.preferences
        .map { it.orpColorIndex }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val fontIndex: StateFlow<Int> = prefsRepo.preferences
        .map { it.fontIndex }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val fontSize: StateFlow<Int> = prefsRepo.preferences
        .map { it.fontSize }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 46)

    /** 1=single word, 2/3/4=chunk reading. Sourced from DataStore. */
    val chunkSize: StateFlow<Int> = prefsRepo.preferences
        .map { it.chunkSize }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1)

    val sentencePauseMultiplier: StateFlow<Float> = prefsRepo.preferences
        .map { it.sentencePauseMultiplier }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2.0f)

    val clausePauseMultiplier: StateFlow<Float> = prefsRepo.preferences
        .map { it.clausePauseMultiplier }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 1.5f)

    private val _newAchievements = MutableStateFlow<List<String>>(emptyList())
    val newAchievements: StateFlow<List<String>> = _newAchievements.asStateFlow()

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
            val words    = repo.loadWords(bookId)
            val savedWpm = prefsRepo.preferences.first().defaultWpm
            _state.update {
                it.copy(words = words, currentIndex = savedIndex, isLoading = false, wpm = savedWpm)
            }
        }
    }

    /**
     * Returns the current chunk of words to display based on chunkSize.
     * Always returns a list of exactly chunkSize items (padded with empty
     * strings if near end of book). The ORP focal word is the first word
     * in the chunk (index 0 of the returned list).
     */
    fun getCurrentChunk(): List<String> {
        val s     = _state.value
        val chunk = chunkSize.value
        return (0 until chunk).map { offset ->
            s.words.getOrElse(s.currentIndex + offset) { "" }
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
                    Log.w("ReaderViewModel", "Session record failed: \${e.localizedMessage}")
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
                Log.w("ReaderViewModel", "Progress sync failed: \${e.localizedMessage}")
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

    /** Adjust chunk size in reader (+1 or -1), clamped 1–4. */
    fun adjustChunkSize(delta: Int) {
        val newSize = (chunkSize.value + delta).coerceIn(1, 4)
        viewModelScope.launch { prefsRepo.setChunkSize(newSize) }
    }

    fun skipSeconds(seconds: Int) {
        if (seconds < 0) sessionRewindCount++
        val wpm         = _state.value.wpm
        val chunk       = chunkSize.value
        val wordsToSkip = (wpm * abs(seconds) / 60f).roundToInt().coerceAtLeast(chunk)
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
                val s     = _state.value
                val chunk = chunkSize.value
                if (s.currentIndex >= s.words.lastIndex) {
                    _state.update { it.copy(isPlaying = false) }
                    sessionActiveMs += System.currentTimeMillis() - lastPlayStartMs
                    break
                }
                
                // Calculate base delay for this chunk
                val baseDelay = (60_000L * chunk) / s.wpm
                
                // Apply punctuation pause multiplier to the last word in the chunk
                val lastWordInChunk = s.words.getOrNull(s.currentIndex + chunk - 1) ?: ""
                val pauseMultiplier = when {
                    OrpEngine.hasSentenceEndingPunctuation(lastWordInChunk) -> sentencePauseMultiplier.value
                    OrpEngine.hasClausePunctuation(lastWordInChunk) -> clausePauseMultiplier.value
                    else -> 1.0f
                }
                
                val adjustedDelay = (baseDelay * pauseMultiplier).toLong()
                
                delay(adjustedDelay)
                _state.update { it.copy(currentIndex = (it.currentIndex + chunk).coerceAtMost(it.words.lastIndex)) }
            }
        }
    }

    private fun stopPlayback() { playJob?.cancel() }

    override fun onCleared() {
        saveProgress()
        stopPlayback()
    }
}
