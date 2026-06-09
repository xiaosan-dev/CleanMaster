package com.xiaosan.cleanmaster.trash

import com.xiaosan.cleanmaster.core.model.CleanType
import com.xiaosan.cleanmaster.core.model.CleanableItem
import com.xiaosan.cleanmaster.core.model.FileInfo
import com.xiaosan.cleanmaster.core.model.RiskLevel
import com.xiaosan.cleanmaster.core.model.TrashItem
import com.xiaosan.cleanmaster.core.model.FileType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class TrashManagerTest {

    private lateinit var trashDao: TrashDao
    private lateinit var trashDir: File
    private lateinit var config: TrashConfig

    @Before
    fun setup() {
        trashDao = mockk(relaxed = true)
        trashDir = File.createTempFile("trash", "").apply {
            delete()
            mkdirs()
        }
        config = TrashConfig(maxSizeMB = 100)
    }

    @Test
    fun `should move file to trash`(): Unit = runBlocking {
        val sourceFile = File.createTempFile("test", ".txt")
        sourceFile.writeText("test content")

        coEvery { trashDao.getTotalSize() } returns 0L
        coEvery { trashDao.insert(any()) } just Runs

        val manager = TrashManager(trashDao, trashDir.absolutePath, config)
        val item = CleanableItem(
            FileInfo(sourceFile.absolutePath, sourceFile.name, sourceFile.length(), 0, "txt"),
            CleanType.JUNK,
            RiskLevel.LOW
        )

        val result = manager.moveToTrash(listOf(item))
        assertEquals(1, result.movedCount)
        assertFalse(sourceFile.exists())

        sourceFile.delete()
        trashDir.deleteRecursively()
    }

    @Test
    fun `should restore file from trash`(): Unit = runBlocking {
        val originalPath = File.createTempFile("original", ".txt").absolutePath
        val trashFile = File(trashDir, "test.txt")
        trashFile.writeText("restored content")

        coEvery { trashDao.getById(1L) } returns TrashItem(
            id = 1,
            originalPath = originalPath,
            trashPath = trashFile.absolutePath,
            fileName = "test.txt",
            size = 100,
            deletedAt = System.currentTimeMillis(),
            fileType = FileType.JUNK
        )
        coEvery { trashDao.delete(any()) } just Runs

        val manager = TrashManager(trashDao, trashDir.absolutePath, config)
        val trashItem = TrashItem(
            id = 1,
            originalPath = originalPath,
            trashPath = trashFile.absolutePath,
            fileName = "test.txt",
            size = 100,
            deletedAt = System.currentTimeMillis(),
            fileType = FileType.JUNK
        )

        val result = manager.restore(listOf(trashItem))
        assertEquals(1, result.restoredCount)

        File(originalPath).delete()
        trashDir.deleteRecursively()
    }
}
