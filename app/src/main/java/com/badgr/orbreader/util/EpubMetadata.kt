package com.badgr.orbreader.util

import java.util.zip.ZipInputStream

/**
 * Extracts metadata from an EPUB file (which is a ZIP archive).
 * Reads the OPF file to find dc:title and dc:creator without
 * any external dependency — uses only Android's built-in XML parser.
 */
object EpubMetadata {

    data class Meta(val title: String?, val author: String?)

    fun extract(epubBytes: ByteArray): Meta {
        return try {
            // Step 1: find the OPF file path from META-INF/container.xml
            val opfPath = findOpfPath(epubBytes) ?: return Meta(null, null)

            // Step 2: read the OPF file and parse dc:title / dc:creator
            parseOpf(epubBytes, opfPath)
        } catch (e: Exception) {
            Meta(null, null)
        }
    }

    private fun findOpfPath(epubBytes: ByteArray): String? {
        ZipInputStream(epubBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.equals("META-INF/container.xml", ignoreCase = true)) {
                    val content = zip.readBytes().toString(Charsets.UTF_8)
                    // Extract full-path attribute: <rootfile full-path="OEBPS/content.opf" .../>
                    val match = Regex("""full-path=["']([^"']+\.opf)["']""")
                        .find(content)
                    return match?.groupValues?.get(1)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }

    private fun parseOpf(epubBytes: ByteArray, opfPath: String): Meta {
        ZipInputStream(epubBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.equals(opfPath, ignoreCase = true)) {
                    val content = zip.readBytes().toString(Charsets.UTF_8)

                    val title = Regex("""<dc:title[^>]*>([^<]+)</dc:title>""", RegexOption.IGNORE_CASE)
                        .find(content)?.groupValues?.get(1)?.trim()

                    val author = Regex("""<dc:creator[^>]*>([^<]+)</dc:creator>""", RegexOption.IGNORE_CASE)
                        .find(content)?.groupValues?.get(1)?.trim()

                    return Meta(title, author)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return Meta(null, null)
    }
}
