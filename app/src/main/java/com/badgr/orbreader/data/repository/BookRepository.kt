package com.badgr.orbreader.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.badgr.orbreader.data.local.BookDao
import com.badgr.orbreader.data.local.BookEntity
import com.badgr.orbreader.data.model.Book
import com.badgr.orbreader.data.model.FileType
import com.badgr.orbreader.data.remote.ApiClient
import com.badgr.orbreader.util.CoverExtractor
import com.badgr.orbreader.util.EpubMetadata
import com.badgr.orbreader.util.WordTokenizer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

sealed class ImportResult {
    data class Success(val book: Book) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

class BookRepository(
    private val context: Context,
    private val bookDao: BookDao
) {
    private val gson = Gson()

    val books: Flow<List<Book>> = bookDao.getAllBooks().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun importTxt(uri: Uri, fileName: String): ImportResult =
        withContext(Dispatchers.IO) {
            try {
                val text = readTextFromUri(context.contentResolver, uri)
                val words = WordTokenizer.tokenize(text)
                val book = Book(
                    title     = fileName,
                    fileType  = FileType.TXT,
                    wordCount = words.size,
                    coverPath = null
                )
                saveBook(book, words)
                ImportResult.Success(book)
            } catch (e: Exception) {
                ImportResult.Error("Failed to read TXT: ${e.localizedMessage}")
            }
        }

    suspend fun importRemote(
        uri: Uri,
        fileName: String,
        fileType: FileType,
        mimeType: String
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                ?: return@withContext ImportResult.Error("Cannot read file from URI")

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = ApiClient.convertApi.convertFile(part)

            if (!response.isSuccessful) {
                return@withContext ImportResult.Error(
                    "Server error ${response.code()}: ${response.errorBody()?.string()}"
                )
            }

            val body = response.body()
            if (body?.text == null) {
                return@withContext ImportResult.Error(
                    body?.error ?: "Invalid response from server"
                )
            }

            val words = WordTokenizer.tokenize(body.text)

            val tempId = java.util.UUID.randomUUID().toString()

            // ── Cover + metadata extraction ────────────────────────────────
            val coverPath: String?
            val displayTitle: String

            when (fileType) {
                FileType.EPUB -> {
                    coverPath    = CoverExtractor.fromEpub(context, tempId, bytes)
                    val meta     = EpubMetadata.extract(bytes)
                    displayTitle = meta.title?.takeIf { it.isNotBlank() } ?: fileName
                }
                FileType.PDF -> {
                    val tmp  = File(context.cacheDir, "$tempId.pdf").also { it.writeBytes(bytes) }
                    coverPath    = CoverExtractor.fromPdf(context, tempId, tmp).also { tmp.delete() }
                    displayTitle = fileName
                }
                else -> {
                    coverPath    = null
                    displayTitle = fileName
                }
            }

            val book = Book(
                title     = displayTitle,
                fileType  = fileType,
                wordCount = words.size,
                coverPath = coverPath
            )
            saveBook(book, words)
            ImportResult.Success(book)

        } catch (e: Exception) {
            ImportResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    suspend fun loadWords(bookId: String): List<String> = withContext(Dispatchers.IO) {
        val file = wordFile(bookId)
        if (!file.exists()) return@withContext emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<String>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.deleteBookById(book.id)
        wordFile(book.id).delete()
        book.coverPath?.let { File(it).delete() }
    }

    private suspend fun saveBook(book: Book, words: List<String>) {
        wordFile(book.id).writeText(gson.toJson(words))
        bookDao.insertBook(BookEntity.fromDomain(book))
    }

    private fun wordFile(bookId: String): File =
        File(context.filesDir, "words_$bookId.json")

    private fun readTextFromUri(resolver: ContentResolver, uri: Uri): String =
        resolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            ?: throw IllegalStateException("Could not open URI: $uri")
}
