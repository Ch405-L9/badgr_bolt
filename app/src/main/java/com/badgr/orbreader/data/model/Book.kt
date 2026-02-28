package com.badgr.orbreader.data.model

import java.util.UUID

enum class FileType { TXT, PDF, EPUB }

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val fileType: FileType,
    val wordCount: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val coverPath: String? = null   // absolute path to cover image on device storage
)
