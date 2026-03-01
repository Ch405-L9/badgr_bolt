package com.badgr.orbreader.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.repository.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class ReaderUiState(
    val words: List<String> = emptyList(),
    val currentIndex: Int   = 0,
    val wpm: Int            = 150,
    val isPlaying: Boolean  = false,
    val isLoading: Boolean  = true
) {
    val currentWord: String get() = words.getOrElse(currentIndex) { "" }
    val progress: Float     get() = if (words.isEmpty()) 0f
                                    else currentIndex.toFloat() / words.size
}

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db   = BookDatabase.getInstance(application)
    private val repo = BookRepository(context = application, bookDao = db.bookDao())

    private val _state      = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private val _bookTitle  = MutableStateFlow("")
    val bookTitle: StateFlow<String> = _bookTitle.asStateFlow()

    private val _showOrpColor = MutableStateFlow(true)
    val showOrpColor: StateFlow<Boolean> = _showOrpColor.asStateFlow()

    private var currentBookId: String = ""
    private var playJob: Job? = null

    fun loadBook(bookId: String) {
        currentBookId = bookId
        viewModelScope.launch {
            val entity     = db.bookDao().getBookById(bookId)
            val savedIndex = entity?.currentWordIndex ?: 0
            _bookTitle.value = entity?.title ?: ""
            val words = repo.loadWords(bookId)
            _state.update {
                it.copy(words = words, currentIndex = savedIndex, isLoading = false)
            }
        }
    }

    fun saveProgress() {
        if (currentBookId.isEmpty()) return
        val index = _state.value.currentIndex
        viewModelScope.launch {
            db.bookDao().updateProgress(currentBookId, index)
        }
    }

    fun toggleOrpColor(enabled: Boolean) { _showOrpColor.value = enabled }

    fun togglePlayPause() {
        val playing = !_state.value.isPlaying
        _state.update { it.copy(isPlaying = playing) }
        if (playing) startPlayback() else stopPlayback()
    }

    fun adjustWpm(delta: Int) {
        val newWpm = (_state.value.wpm + delta).coerceIn(60, 1200)
        _state.update { it.copy(wpm = newWpm) }
        if (_state.value.isPlaying) { stopPlayback(); startPlayback() }
    }

    /**
     * Skip forward or backward by [seconds] seconds based on current WPM.
     * e.g. at 150 WPM, skipSeconds(10) jumps ahead 25 words.
     * e.g. at 300 WPM, skipSeconds(-10) goes back 50 words.
     */
    fun skipSeconds(seconds: Int) {
        val wpm          = _state.value.wpm
        val wordsToSkip  = (wpm * abs(seconds) / 60f).roundToInt().coerceAtLeast(1)
        val delta        = if (seconds > 0) wordsToSkip else -wordsToSkip
        val newIndex     = (_state.value.currentIndex + delta)
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
