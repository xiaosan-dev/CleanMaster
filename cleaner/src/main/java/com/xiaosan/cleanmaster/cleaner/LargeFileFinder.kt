package com.xiaosan.cleanmaster.cleaner

import com.xiaosan.cleanmaster.core.model.CleanType
import com.xiaosan.cleanmaster.core.model.CleanableItem
import com.xiaosan.cleanmaster.core.model.FileInfo
import com.xiaosan.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class LargeFileFinder(
    private val rootPath: String,
    private val thresholdMB: Int = 100
) : Cleaner {

    private var items: List<CleanableItem> = emptyList()
    private val thresholdBytes = thresholdMB.toLong() * 1024 * 1024

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val largeItems = mutableListOf<CleanableItem>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && file.length() > thresholdBytes) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    largeItems.add(CleanableItem(fileInfo, CleanType.LARGE_FILE, RiskLevel.MEDIUM))
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, largeItems.size, largeItems.sumOf { it.fileInfo.size }))
                }
            }
        }

        items = largeItems
        emit(CleanProgress(scannedCount, largeItems.size, largeItems.sumOf { it.fileInfo.size }))
    }

    override fun getItems(): List<CleanableItem> = items

    override suspend fun clean(items: List<CleanableItem>): CleanResult {
        var cleanedCount = 0
        var freedSize = 0L
        val errors = mutableListOf<String>()

        withContext(Dispatchers.IO) {
            items.forEach { item ->
                try {
                    val file = File(item.fileInfo.path)
                    if (file.exists()) {
                        freedSize += file.length()
                        file.delete()
                        cleanedCount++
                    }
                } catch (e: Exception) {
                    errors.add("Failed to delete ${item.fileInfo.path}: ${e.message}")
                }
            }
        }

        return CleanResult(cleanedCount, freedSize, errors)
    }
}
