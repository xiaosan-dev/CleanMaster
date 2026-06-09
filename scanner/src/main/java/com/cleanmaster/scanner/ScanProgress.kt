package com.cleanmaster.scanner

data class ScanProgress(
    val scannedCount: Int,
    val currentPath: String,
    val foundSize: Long
)
