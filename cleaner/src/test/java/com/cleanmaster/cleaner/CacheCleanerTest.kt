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

class CacheCleanerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        File(cacheDir, "data.cache").writeText("cache content")

        val cleaner = CacheCleaner(tempFolder.root.absolutePath)
        cleaner.scan().toList()
        val items = cleaner.getItems()

        assertTrue(items.isNotEmpty())
        assertEquals(CleanType.CACHE, items[0].type)
        assertEquals(RiskLevel.LOW, items[0].riskLevel)
    }

    @Test
    fun `should clean cache files`() = runBlocking {
        val cacheDir = tempFolder.newFolder("cache")
        val cacheFile = File(cacheDir, "data.cache")
        cacheFile.writeText("cache content")

        val cleaner = CacheCleaner(tempFolder.root.absolutePath)
        cleaner.scan().toList()
        val items = cleaner.getItems()
        val result = cleaner.clean(items)

        assertEquals(1, result.cleanedCount)
        assertFalse(cacheFile.exists())
    }
}
