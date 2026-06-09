package com.xiaosan.cleanmaster.core.util

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class HashUtilTest {

    @Test
    fun `should calculate consistent hash for same file`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("hello world")
        val hash1 = HashUtil.calculateSha256(file)
        val hash2 = HashUtil.calculateSha256(file)
        assertEquals(hash1, hash2)
        file.delete()
    }

    @Test
    fun `should calculate different hash for different content`() {
        val file1 = File.createTempFile("test1", ".txt")
        val file2 = File.createTempFile("test2", ".txt")
        file1.writeText("hello")
        file2.writeText("world")
        assertNotEquals(HashUtil.calculateSha256(file1), HashUtil.calculateSha256(file2))
        file1.delete()
        file2.delete()
    }

    @Test
    fun `should return 64 character hex string`() {
        val file = File.createTempFile("test", ".txt")
        file.writeText("test content")
        val hash = HashUtil.calculateSha256(file)
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
        file.delete()
    }
}
