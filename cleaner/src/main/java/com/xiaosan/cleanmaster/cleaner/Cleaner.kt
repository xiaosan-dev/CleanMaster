package com.xiaosan.cleanmaster.cleaner

import com.xiaosan.cleanmaster.core.model.CleanableItem
import kotlinx.coroutines.flow.Flow

data class CleanProgress(
    val scannedCount: Int,
    val foundCount: Int,
    val foundSize: Long
)

data class CleanResult(
    val cleanedCount: Int,
    val freedSize: Long,
    val errors: List<String>
)

interface Cleaner {
    suspend fun scan(): Flow<CleanProgress>
    fun getItems(): List<CleanableItem>
    suspend fun clean(items: List<CleanableItem>): CleanResult
}
