package com.xiaosan.cleanmaster.trash

data class TrashConfig(
    val maxSizeMB: Int = 2048,
    val autoCleanWhenFull: Boolean = true,
    val confirmPermanentDelete: Boolean = true
)
