package com.badgr.orbreader.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.badgr.orbreader.data.model.Book
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BookRow(
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(book.createdAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            .format(Date(book.createdAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Cover thumbnail ───────────────────────────────────────────────
        val coverFile = book.coverPath?.let { File(it) }
        if (coverFile != null && coverFile.exists()) {
            AsyncImage(
                model          = coverFile,
                contentDescription = "Cover of ${book.title}",
                contentScale   = ContentScale.Crop,
                modifier       = Modifier
                    .size(width = 48.dp, height = 68.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            // Placeholder when no cover is available
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 68.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Using AutoMirrored MenuBook which is standard in Material3 icons
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }

        // ── Title + metadata ──────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = book.title,
                style      = MaterialTheme.typography.bodyLarge,
                maxLines   = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = "${book.fileType.name} · ${book.wordCount} words · $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Delete ────────────────────────────────────────────────────────
        IconButton(onClick = onDelete) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Delete ${book.title}"
            )
        }
    }
}
