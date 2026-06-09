package com.xiaosan.cleanmaster.scanner

import com.xiaosan.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner : Scanner<List<FileInfo>> {

    private var result: List<FileInfo> = emptyList()

    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
    private val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp")
    private val allMediaExtensions = imageExtensions + videoExtensions

    override suspend fun scan(rootPath: String): Flow<ScanProgress> = flow {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            result = emptyList()
            return@flow
        }

        val mediaFiles = mutableListOf<FileInfo>()
        var scannedCount = 0

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++
                if (file.isFile && file.extension.lowercase() in allMediaExtensions) {
                    mediaFiles.add(
                        FileInfo(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            mimeType = file.extension
                        )
                    )
                }

                if (scannedCount % 100 == 0) {
                    emit(ScanProgress(scannedCount, file.absolutePath, mediaFiles.sumOf { it.size }))
                }
            }
        }

        result = mediaFiles
        emit(ScanProgress(scannedCount, rootPath, mediaFiles.sumOf { it.size }))
    }

    override fun getResult(): List<FileInfo> = result
}
