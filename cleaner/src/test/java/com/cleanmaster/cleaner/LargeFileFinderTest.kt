package com.cleanmaster.cleaner

import com.cleanmaster.core.model.CleanType
import com.cleanmaster.core.model.RiskLevel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LargeFileFinderTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find files larger than threshold`() = runBlocking {
        val largeFile = File(tempFolder.root, "large.dat")
        largeFile.writeBytes(ByteArray(200 * 1024 * 1024)) // 200MB

        val finder = LargeFileFinder(tempFolder.root.absolutePath, thresholdMB = 100)
        finder.scan().toList()
        val items = finder.getItems()

        assertEquals(1, items.size)
        assertEquals(CleanType.LARGE_FILE, items[0].type)
        assertEquals(RiskLevel.MEDIUM, items[0].riskLevel)
    }

    @Test
    fun `should not find files smaller than threshold`() = runBlocking {
        val smallFile = File(tempFolder.root, "small.dat")
        smallFile.writeBytes(ByteArray(50 * 1024 * 1024)) // 50MB

        val finder = LargeFileFinder(tempFolder.root.absolutePath, thresholdMB = 100)
        finder.scan().toList()
        val items = finder.getItems()

        assertTrue(items.isEmpty())
    }
}
