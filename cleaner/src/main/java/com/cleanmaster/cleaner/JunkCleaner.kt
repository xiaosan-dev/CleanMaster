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

class JunkCleaner(private val rootPath: String) : Cleaner {

    private var items: List<CleanableItem> = emptyList()
    private val junkExtensions = setOf(".tmp", ".log", ".bak", ".temp", ".swp")

    override suspend fun scan(): Flow<CleanProgress> = flow {
        val root = File(rootPath)
        val junkItems = mutableListOf<CleanableItem>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && ".${file.extension.lowercase()}" in junkExtensions) {
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        mimeType = file.extension
                    )
                    junkItems.add(CleanableItem(fileInfo, CleanType.JUNK, RiskLevel.LOW))
                }

                if (scannedCount % 100 == 0) {
                    emit(CleanProgress(scannedCount, junkItems.size, junkItems.sumOf { it.fileInfo.size }))
                }
            }
        }

        items = junkItems
        emit(CleanProgress(scannedCount, junkItems.size, junkItems.sumOf { it.fileInfo.size }))
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
