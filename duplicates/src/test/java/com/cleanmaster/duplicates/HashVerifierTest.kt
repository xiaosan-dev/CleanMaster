package com.cleanmaster.duplicates

import com.cleanmaster.core.model.DuplicateGroup
import com.cleanmaster.core.model.FileInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HashVerifierTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should verify exact duplicates`() = runBlocking {
        val file1 = File(tempFolder.root, "a.txt")
        val file2 = File(tempFolder.root, "b.txt")
        file1.writeText("same content")
        file2.writeText("same content")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "b.txt", file2.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertEquals(1, groups.size)
        assertEquals(2, groups[0].files.size)
    }

    @Test
    fun `should reject files with different content`() = runBlocking {
        val file1 = File(tempFolder.root, "a.txt")
        val file2 = File(tempFolder.root, "b.txt")
        file1.writeText("content A")
        file2.writeText("content B")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "b.txt", file2.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should handle multiple groups`() = runBlocking {
        val file1 = File(tempFolder.root, "a1.txt")
        val file2 = File(tempFolder.root, "a2.txt")
        val file3 = File(tempFolder.root, "b1.txt")
        val file4 = File(tempFolder.root, "b2.txt")
        file1.writeText("group A")
        file2.writeText("group A")
        file3.writeText("group B")
        file4.writeText("group B")

        val candidates = listOf(
            listOf(
                FileInfo(file1.absolutePath, "a1.txt", file1.length(), 0, "txt"),
                FileInfo(file2.absolutePath, "a2.txt", file2.length(), 0, "txt")
            ),
            listOf(
                FileInfo(file3.absolutePath, "b1.txt", file3.length(), 0, "txt"),
                FileInfo(file4.absolutePath, "b2.txt", file4.length(), 0, "txt")
            )
        )

        val groups = HashVerifier.verify(candidates)
        assertEquals(2, groups.size)
    }
}
