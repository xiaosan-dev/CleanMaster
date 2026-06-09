package com.xiaosan.cleanmaster.core.model

enum class FileType {
    CACHE, JUNK, LARGE_FILE, DUPLICATE, MEDIA, EMPTY_FOLDER
}

data class DuplicateGroup(
    val id: String,
    val files: List<FileInfo>,
    val totalSize: Long,
    val fileHash: String
)
