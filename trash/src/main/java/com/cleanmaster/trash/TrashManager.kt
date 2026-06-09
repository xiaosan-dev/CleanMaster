package com.cleanmaster.trash

import com.cleanmaster.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class TrashResult(val movedCount: Long, val totalSize: Long)
data class RestoreResult(val restoredCount: Int, val errors: List<String>)
data class DeleteResult(val deletedCount: Int, val freedSize: Long)
data class TrashInfo(val itemCount: Int, val totalSize: Long, val oldestItemDate: Long)

class TrashManager(
    private val trashDao: TrashDao,
    private val trashDirPath: String,
    private val config: TrashConfig
) {
    private val trashDir = File(trashDirPath)

    init {
        if (!trashDir.exists()) {
            trashDir.mkdirs()
        }
    }

    suspend fun moveToTrash(items: List<CleanableItem>): TrashResult = withContext(Dispatchers.IO) {
        var movedCount = 0L
        var totalSize = 0L

        items.forEach { item ->
            try {
                val source = File(item.fileInfo.path)
                if (!source.exists()) return@forEach

                val trashFile = File(trashDir, "${UUID.randomUUID()}_${item.fileInfo.name}")
                source.copyTo(trashFile, overwrite = true)
                source.delete()

                val trashItem = TrashItem(
                    originalPath = item.fileInfo.path,
                    trashPath = trashFile.absolutePath,
                    fileName = item.fileInfo.name,
                    size = item.fileInfo.size,
                    deletedAt = System.currentTimeMillis(),
                    fileType = when (item.type) {
                        CleanType.CACHE -> FileType.CACHE
                        CleanType.JUNK -> FileType.JUNK
                        CleanType.LARGE_FILE -> FileType.LARGE_FILE
                        CleanType.EMPTY_FOLDER -> FileType.EMPTY_FOLDER
                        CleanType.MEDIA -> FileType.MEDIA
                        CleanType.SCREENSHOT -> FileType.DUPLICATE
                    }
                )

                trashDao.insert(trashItem)
                movedCount++
                totalSize += item.fileInfo.size
            } catch (e: Exception) {
                // Skip files that fail
            }
        }

        autoCleanIfNeeded()
        TrashResult(movedCount, totalSize)
    }

    suspend fun restore(items: List<TrashItem>): RestoreResult = withContext(Dispatchers.IO) {
        var restoredCount = 0
        val errors = mutableListOf<String>()

        items.forEach { item ->
            try {
                val trashFile = File(item.trashPath)
                if (!trashFile.exists()) {
                    errors.add("Trash file not found: ${item.trashPath}")
                    return@forEach
                }

                val originalFile = File(item.originalPath)
                originalFile.parentFile?.mkdirs()
                trashFile.copyTo(originalFile, overwrite = true)
                trashFile.delete()
                trashDao.delete(item)
                restoredCount++
            } catch (e: Exception) {
                errors.add("Failed to restore ${item.fileName}: ${e.message}")
            }
        }

        RestoreResult(restoredCount, errors)
    }

    suspend fun permanentDelete(items: List<TrashItem>): DeleteResult = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var freedSize = 0L

        items.forEach { item ->
            try {
                val trashFile = File(item.trashPath)
                if (trashFile.exists()) {
                    freedSize += trashFile.length()
                    trashFile.delete()
                }
                trashDao.delete(item)
                deletedCount++
            } catch (e: Exception) {
                // Skip files that fail
            }
        }

        DeleteResult(deletedCount, freedSize)
    }

    suspend fun emptyTrash(): DeleteResult = withContext(Dispatchers.IO) {
        val items = trashDao.getAllItems().let { flow ->
            var result = emptyList<TrashItem>()
            flow.collect { result = it }
            result
        }
        permanentDelete(items)
    }

    suspend fun getTrashInfo(): TrashInfo {
        val itemCount = trashDao.getItemCount()
        val totalSize = trashDao.getTotalSize() ?: 0L
        val oldestItem = trashDao.getOldestItem()
        return TrashInfo(itemCount, totalSize, oldestItem?.deletedAt ?: 0L)
    }

    private suspend fun autoCleanIfNeeded() {
        if (!config.autoCleanWhenFull) return

        val currentSize = trashDao.getTotalSize() ?: 0L
        val maxSize = config.maxSizeMB.toLong() * 1024 * 1024

        if (currentSize > maxSize) {
            val oldest = trashDao.getOldestItem() ?: return
            permanentDelete(listOf(oldest))
        }
    }
}
