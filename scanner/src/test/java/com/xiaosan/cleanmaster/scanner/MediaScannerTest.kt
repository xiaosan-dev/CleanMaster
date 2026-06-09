package com.xiaosan.cleanmaster.scanner

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MediaScannerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `should find image files`() = runBlocking {
        File(tempFolder.root, "photo.jpg").writeBytes(ByteArray(100))
        File(tempFolder.root, "screenshot.png").writeBytes(ByteArray(100))
        File(tempFolder.root, "document.txt").writeText("not media")

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.size)
    }

    @Test
    fun `should find video files`() = runBlocking {
        File(tempFolder.root, "video.mp4").writeBytes(ByteArray(100))
        File(tempFolder.root, "clip.mov").writeBytes(ByteArray(100))

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertEquals(2, result.size)
    }

    @Test
    fun `should return empty list for no media`() = runBlocking {
        File(tempFolder.root, "text.txt").writeText("hello")

        val scanner = MediaScanner()
        scanner.scan(tempFolder.root.absolutePath).toList()
        val result = scanner.getResult()

        assertTrue(result.isEmpty())
    }
}
