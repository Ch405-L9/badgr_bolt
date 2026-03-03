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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.badgr.orbreader.ui.theme.ReaderColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenBook: (bookId: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val books   by viewModel.books.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // ── File pickers ──────────────────────────────────────────────────────
    val txtPicker   = rememberFilePicker("text/plain")             { u, n -> viewModel.importTxt(u, n)  }
    val pdfPicker   = rememberFilePicker("application/pdf")        { u, n -> viewModel.importPdf(u, n)  }
    val epubPicker  = rememberFilePicker("application/epub+zip")   { u, n -> viewModel.importEpub(u, n) }
    val docxPicker  = rememberFilePicker("application/vnd.openxmlformats-officedocument.wordprocessingml.document") 
                                                                    { u, n -> viewModel.importDocx(u, n) }
    val imagePicker = rememberFilePicker("image/*")               { u, n -> viewModel.importImage(u, n) }

    // ── Error dialog ──────────────────────────────────────────────────────
    if (uiState is LibraryUiState.Error) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Import failed") },
            text  = { Text((uiState as LibraryUiState.Error).message) },
            confirmButton   = { TextButton(onClick = viewModel::clearError) { Text("OK") } }
        )
    }

    Scaffold(
        containerColor = ReaderColors.background,
        topBar = {
            TopAppBar(
                title  = { Text("BADGR Bolt Library", color = ReaderColors.textWarm) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReaderColors.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Import buttons row 1 ───────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImportBtn("TXT",  Modifier.weight(1f), "Import text file")   { txtPicker.launch("text/plain") }
                ImportBtn("PDF",  Modifier.weight(1f), "Import PDF file")    { pdfPicker.launch("application/pdf") }
                ImportBtn("EPUB", Modifier.weight(1f), "Import EPUB file")   { epubPicker.launch("application/epub+zip") }
            }
            // ── Import buttons row 2 ───────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImportBtn("DOCX",  Modifier.weight(1f), "Import Word document") {
                    docxPicker.launch("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                }
                ImportBtn("Image", Modifier.weight(1f), "Import image for OCR") {
                    imagePicker.launch("image/*")
                }
            }

            if (uiState is LibraryUiState.Converting) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = ReaderColors.orpFocal
                    )
                    Text(
                        "Converting \"${(uiState as LibraryUiState.Converting).fileName}\"…",
                        color = ReaderColors.textDimmed
                    )
                }
            }

            HorizontalDivider(color = ReaderColors.guideLine)

            if (books.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No books yet. Import a file above.", color = ReaderColors.textDimmed)
                }
            } else {
                LazyColumn {
                    items(books, key = { it.id }) { book ->
                        BookRow(
                            book     = book,
                            onClick  = { onOpenBook(book.id) },
                            onDelete = { viewModel.deleteBook(book) }
                        )
                        HorizontalDivider(color = ReaderColors.guideLine.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportBtn(
    label       : String,
    modifier    : Modifier,
    description : String,
    onClick     : () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.semantics { contentDescription = description },
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = ReaderColors.orpFocal)
    ) { Text("+ $label") }
}

@Composable
private fun rememberFilePicker(
    mimeType: String,
    onPicked: (uri: Uri, fileName: String) -> Unit
): ManagedLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = resolveFileName(context.contentResolver, uri) ?: "document"
        onPicked(uri, name)
    }
    return remember(launcher) { ManagedLauncher(launcher) }
}

private class ManagedLauncher(private val l: ActivityResultLauncher<String>) {
    fun launch(mime: String) = l.launch(mime)
}

private fun resolveFileName(resolver: ContentResolver, uri: Uri): String? {
    resolver.query(uri, null, null, null, null)?.use { c ->
        if (c.moveToFirst()) {
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) return c.getString(idx)
        }
    }
    return uri.lastPathSegment
}
