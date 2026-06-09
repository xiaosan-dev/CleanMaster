package com.cleanmaster.core.model

data class ScanResult(
    val cacheFiles: List<FileInfo>,
    val junkFiles: List<FileInfo>,
    val largeFiles: List<FileInfo>,
    val emptyFolders: List<String>,
    val duplicateGroups: List<DuplicateGroup>,
    val mediaFiles: List<FileInfo>,
    val totalSize: Long
)
