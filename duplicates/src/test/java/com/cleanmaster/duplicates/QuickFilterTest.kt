package com.cleanmaster.duplicates

import com.cleanmaster.core.model.FileInfo
import org.junit.Assert.*
import org.junit.Test

class QuickFilterTest {

    @Test
    fun `should group files with same name and size`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/c/photo.jpg", "photo.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertEquals(1, groups.size)
        assertEquals(3, groups[0].size)
    }

    @Test
    fun `should not group files with different size`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 2048, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should not group files with different name`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/image.jpg", "image.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should return empty list for single files`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg")
        )

        val groups = QuickFilter.filter(files)
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `should return multiple groups`() {
        val files = listOf(
            FileInfo("/a/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/b/photo.jpg", "photo.jpg", 1024, 0, "jpg"),
            FileInfo("/a/video.mp4", "video.mp4", 2048, 0, "mp4"),
            FileInfo("/b/video.mp4", "video.mp4", 2048, 0, "mp4")
        )

        val groups = QuickFilter.filter(files)
        assertEquals(2, groups.size)
    }
}
