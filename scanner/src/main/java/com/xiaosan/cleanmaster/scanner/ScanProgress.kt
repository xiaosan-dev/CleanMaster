package com.xiaosan.cleanmaster.scanner

data class ScanProgress(
    val scannedCount: Int,
    val currentPath: String,
    val foundSize: Long
)
