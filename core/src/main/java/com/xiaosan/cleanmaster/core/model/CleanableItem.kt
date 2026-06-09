package com.xiaosan.cleanmaster.core.model

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

enum class CleanType {
    CACHE, JUNK, LARGE_FILE, EMPTY_FOLDER, MEDIA, SCREENSHOT
}

data class CleanableItem(
    val fileInfo: FileInfo,
    val type: CleanType,
    val riskLevel: RiskLevel
)
