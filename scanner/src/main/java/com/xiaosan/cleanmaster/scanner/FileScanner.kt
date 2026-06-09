package com.xiaosan.cleanmaster.scanner

import com.xiaosan.cleanmaster.core.model.FileInfo
import com.xiaosan.cleanmaster.core.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File

class FileScanner : Scanner<ScanResult> {

    private var result: ScanResult? = null

    private val cacheDirs = listOf("cache", "code_cache")
    private val junkExtensions = setOf(".tmp", ".log", ".bak", ".temp", ".swp")

    override suspend fun scan(rootPath: String): Flow<ScanProgress> = flow {
        val root = File(rootPath)
        if (!root.exists() || !root.isDirectory) {
            result = ScanResult(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0)
            return@flow
        }

        val cacheFiles = mutableListOf<FileInfo>()
        val junkFiles = mutableListOf<FileInfo>()
        val emptyFolders = mutableListOf<String>()
        var scannedCount = 0
        var totalSize = 0L

        withContext(Dispatchers.IO) {
            root.walkTopDown().forEach { file ->
                scannedCount++

                when {
                    file.isDirectory && file.listFiles().isNullOrEmpty() && file != root -> {
                        emptyFolders.add(file.absolutePath)
                    }
                    file.isFile -> {
                        val isCache = cacheDirs.any { file.absolutePath.contains(it, ignoreCase = true) }
                        if (isCache) {
                            cacheFiles.add(file.toFileInfo())
                            totalSize += file.length()
                        }

                        val ext = file.extension.lowercase()
                        if (file.parentFile?.name != "cache" && ".$ext" in junkExtensions) {
                            junkFiles.add(file.toFileInfo())
                            totalSize += file.length()
                        }
                    }
                }

                if (scannedCount % 100 == 0) {
                    emit(ScanProgress(scannedCount, file.absolutePath, totalSize))
                }
            }
        }

        result = ScanResult(
            cacheFiles = cacheFiles,
            junkFiles = junkFiles,
            largeFiles = emptyList(),
            emptyFolders = emptyFolders,
            duplicateGroups = emptyList(),
            mediaFiles = emptyList(),
            totalSize = totalSize
        )

        emit(ScanProgress(scannedCount, rootPath, totalSize))
    }

    override fun getResult(): ScanResult = result ?: ScanResult(
        emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), 0
    )

    private fun File.toFileInfo() = FileInfo(
        path = absolutePath,
        name = name,
        size = length(),
        lastModified = lastModified(),
        mimeType = extension
    )
}
