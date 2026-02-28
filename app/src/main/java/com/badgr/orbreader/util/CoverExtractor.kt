package com.badgr.orbreader.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/**
 * Extracts or generates a cover image file for a book and saves it to
 * the app's internal files/covers/ directory.
 *
 * Returns the absolute path to the saved PNG, or null if extraction failed.
 */
object CoverExtractor {

    /** Pull the first image listed as "cover" in an EPUB (it is just a ZIP). */
    fun fromEpub(context: Context, bookId: String, epubBytes: ByteArray): String? {
        return try {
            val coverDir = coversDir(context)
            val outFile  = File(coverDir, "$bookId.png")

            ZipInputStream(epubBytes.inputStream()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name.lowercase()
                    // EPUB cover is usually in OEBPS/images/ and named cover.*
                    if (!entry.isDirectory &&
                        (name.contains("cover") || name.contains("thumbnail")) &&
                        (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                         name.endsWith(".png") || name.endsWith(".webp"))
                    ) {
                        FileOutputStream(outFile).use { out -> zip.copyTo(out) }
                        return outFile.absolutePath
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            null   // no cover image found in EPUB
        } catch (e: Exception) {
            null
        }
    }

    /** Render page 0 of a PDF as a 200x280 thumbnail. */
    fun fromPdf(context: Context, bookId: String, pdfFile: File): String? {
        return try {
            val coverDir = coversDir(context)
            val outFile  = File(coverDir, "$bookId.png")

            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            PdfRenderer(pfd).use { renderer ->
                renderer.openPage(0).use { page ->
                    val bmp = Bitmap.createBitmap(200, 280, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    FileOutputStream(outFile).use { out ->
                        bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
                    }
                    bmp.recycle()
                }
            }
            outFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Save a user-picked image URI as the cover for a book. */
    fun fromUserPick(context: Context, bookId: String, imageBytes: ByteArray): String? {
        return try {
            val outFile = File(coversDir(context), "$bookId.png")
            outFile.writeBytes(imageBytes)
            outFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun coversDir(context: Context): File {
        val dir = File(context.filesDir, "covers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
