package com.badgr.orbreader.ui.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.badgr.orbreader.data.local.BookDatabase
import com.badgr.orbreader.data.model.Book
import com.badgr.orbreader.data.model.FileType
import com.badgr.orbreader.data.repository.BookRepository
import com.badgr.orbreader.data.repository.ImportResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class LibraryUiState {
    data object Idle : LibraryUiState()
    data class Converting(val fileName: String) : LibraryUiState()
    data class Error(val message: String) : LibraryUiState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BookRepository(
        context = application,
        bookDao = BookDatabase.getInstance(application).bookDao()
    )

    val books: StateFlow<List<Book>> = repo.books
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Idle)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun importTxt(uri: Uri, name: String)   = launch(name) { repo.importTxt(uri, name) }
    fun importPdf(uri: Uri, name: String)   = launch(name) { repo.importRemote(uri, name, FileType.PDF,   "application/pdf") }
    fun importEpub(uri: Uri, name: String)  = launch(name) { repo.importRemote(uri, name, FileType.EPUB,  "application/epub+zip") }
    fun importDocx(uri: Uri, name: String)  = launch(name) {
        repo.importRemote(
            uri, name, FileType.DOCX,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    }
    fun importImage(uri: Uri, name: String) = launch(name) {
        repo.importRemote(uri, name, FileType.IMAGE, "image/*")
    }

    fun deleteBook(book: Book) = viewModelScope.launch { repo.deleteBook(book) }
    fun clearError()           { _uiState.value = LibraryUiState.Idle }

    private fun launch(fileName: String, block: suspend () -> ImportResult) {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Converting(fileName)
            _uiState.value = when (val r = block()) {
                is ImportResult.Success -> LibraryUiState.Idle
                is ImportResult.Error   -> LibraryUiState.Error(r.message)
            }
        }
    }
}
