package com.cleanmaster.scanner

import com.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        File(cacheDir, "test.cache").writeText("cache data")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.cacheFiles.isNotEmpty())
        assertEquals("test.cache", result.cacheFiles[0].name)
    }

    @Test
    fun `should find junk files by extension`() = runBlocking {
        File(tempFolder.root, "temp.tmp").writeText("temp data")
        File(tempFolder.root, "log.log").writeText("log data")
        File(tempFolder.root, "backup.bak").writeText("backup data")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(3, result.junkFiles.size)
    }

    @Test
    fun `should find empty folders`() = runBlocking {
        tempFolder.newFolder("empty1")
        tempFolder.newFolder("empty2")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.emptyFolders.size)
    }

    @Test
    fun `should not count non-empty folders as empty`() = runBlocking {
        val dir = tempFolder.newFolder("notempty")
        File(dir, "file.txt").writeText("content")

        val scanner = FileScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.emptyFolders.none { it.contains("notempty") })
    }
}
