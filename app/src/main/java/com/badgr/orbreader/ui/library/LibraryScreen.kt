package com.badgr.orbreader.ui.library

import android.net.Uri
import android.content.ContentResolver
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenBook: (bookId: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val books   by viewModel.books.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ── File pickers ──────────────────────────────────────────────────────────
    val txtPicker = rememberFilePicker("text/plain") { uri, name ->
        viewModel.importTxt(uri, name)
    }
    val pdfPicker = rememberFilePicker("application/pdf") { uri, name ->
        viewModel.importPdf(uri, name)
    }
    val epubPicker = rememberFilePicker("application/epub+zip") { uri, name ->
        viewModel.importEpub(uri, name)
    }

    // ── Error dialog ──────────────────────────────────────────────────────────
    if (uiState is LibraryUiState.Error) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Import failed") },
            text = { Text((uiState as LibraryUiState.Error).message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("OrbReader Library") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Import buttons ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImportButton(label = "TXT", modifier = Modifier.weight(1f)) {
                    txtPicker.launch("text/plain")
                }
                ImportButton(label = "PDF", modifier = Modifier.weight(1f)) {
                    pdfPicker.launch("application/pdf")
                }
                ImportButton(label = "EPUB", modifier = Modifier.weight(1f)) {
                    epubPicker.launch("application/epub+zip")
                }
            }

            // ── Converting indicator ───────────────────────────────────────
            if (uiState is LibraryUiState.Converting) {
                val name = (uiState as LibraryUiState.Converting).fileName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Converting \"$name\"…",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            HorizontalDivider()

            // ── Book list ──────────────────────────────────────────────────
            if (books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No books yet. Import a TXT, PDF, or EPUB file.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn {
                    items(books, key = { it.id }) { book ->
                        BookRow(
                            book = book,
                            onClick = { onOpenBook(book.id) },
                            onDelete = { viewModel.deleteBook(book) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ImportButton(
    label: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(text = "+ $label")
    }
}

// ── Helper: file picker launcher ──────────────────────────────────────────────

@Composable
private fun rememberFilePicker(
    mimeType: String,
    onPicked: (uri: Uri, fileName: String) -> Unit
): ManagedActivityResultLauncherWrapper {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = resolveFileName(context.contentResolver, uri) ?: "Unknown"
        onPicked(uri, name)
    }
    return remember(launcher) { ManagedActivityResultLauncherWrapper(launcher) }
}

private class ManagedActivityResultLauncherWrapper(
    private val launcher: ActivityResultLauncher<String>
) {
    fun launch(mimeType: String) = launcher.launch(mimeType)
}

private fun resolveFileName(resolver: ContentResolver, uri: Uri): String? {
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) return cursor.getString(idx)
        }
    }
    return uri.lastPathSegment
}
