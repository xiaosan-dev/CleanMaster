package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.CleanableItem
import com.cleanmaster.core.model.FileInfo
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class CacheCleaner(private val rootPath: String) : Cleaner {

    private var items: List<CleanableItem> = emptyList()

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val cacheItems = mutableListOf<CleanableItem>()
        var scannedCount = 0
        var totalSize = 0L

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && (file.parentFile?.name?.contains("cache", ignoreCase = true) == true)) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    cacheItems.add(CleanableItem(fileInfo, CleanType.CACHE, RiskLevel.LOW))
                    totalSize += file.length()
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, cacheItems.size, totalSize))
                }
            }
        }

        items = cacheItems
        emit(CleanProgress(scannedCount, cacheItems.size, totalSize))
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
