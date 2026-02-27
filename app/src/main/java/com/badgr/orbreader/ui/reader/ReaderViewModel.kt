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

data class ReaderUiState(
    val words: List<String> = emptyList(),
    val currentIndex: Int   = 0,
    val wpm: Int            = 300,
    val isPlaying: Boolean  = false,
    val isLoading: Boolean  = true
) {
    val currentWord: String get() = words.getOrElse(currentIndex) { "" }
    val progress: Float     get() = if (words.isEmpty()) 0f else currentIndex.toFloat() / words.size
}

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db   = BookDatabase.getInstance(application)
    private val repo = BookRepository(context = application, bookDao = db.bookDao())

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    // T03 — book title for TopAppBar
    private val _bookTitle = MutableStateFlow("")
    val bookTitle: StateFlow<String> = _bookTitle.asStateFlow()

    // T01 — ORP color toggle (default on)
    private val _showOrpColor = MutableStateFlow(true)
    val showOrpColor: StateFlow<Boolean> = _showOrpColor.asStateFlow()

    private var currentBookId: String = ""
    private var playJob: Job? = null

    fun loadBook(bookId: String) {
        currentBookId = bookId
        viewModelScope.launch {
            // Load title + saved word index from DB
            val entity = db.bookDao().getBookById(bookId)
            val savedIndex = entity?.currentWordIndex ?: 0
            _bookTitle.value = entity?.title ?: ""

            val words = repo.loadWords(bookId)
            _state.update {
                it.copy(words = words, currentIndex = savedIndex, isLoading = false)
            }
        }
    }

    // T02 — persist word position to Room
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
