package com.xiaosan.cleanmaster.core.model

data class ScreenshotInfo(
    val fileInfo: FileInfo,
    val width: Int,
    val height: Int,
    val phash: String? = null,
    val blurScore: Float? = null,
    val isBlackScreen: Boolean = false,
    val isSolidColor: Boolean = false
)

data class SimilarGroup(
    val screenshots: List<ScreenshotInfo>,
    val similarity: Float
)

data class ExpiredScreenshot(
    val screenshot: ScreenshotInfo,
    val ageInDays: Int
)

data class ScreenshotAnalysisResult(
    val duplicates: List<DuplicateGroup>,
    val similar: List<SimilarGroup>,
    val lowQuality: List<ScreenshotInfo>,
    val expired: List<ExpiredScreenshot>,
    val totalCount: Int,
    val recoverableSize: Long
)
