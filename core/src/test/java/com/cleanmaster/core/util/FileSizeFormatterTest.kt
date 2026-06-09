package com.cleanmaster.core.util

import org.junit.Assert.*
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun `should format bytes correctly`() {
        assertEquals("500 B", FileSizeFormatter.format(500))
    }

    @Test
    fun `should format kilobytes correctly`() {
        assertEquals("1.5 KB", FileSizeFormatter.format(1536))
    }

    @Test
    fun `should format megabytes correctly`() {
        assertEquals("2.3 MB", FileSizeFormatter.format(2411724))
    }

    @Test
    fun `should format gigabytes correctly`() {
        assertEquals("1.2 GB", FileSizeFormatter.format(1288490188))
    }

    @Test
    fun `should handle zero`() {
        assertEquals("0 B", FileSizeFormatter.format(0))
    }
}
