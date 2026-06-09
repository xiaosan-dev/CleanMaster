package com.xiaosan.cleanmaster.duplicates

import org.junit.Assert.*
import org.junit.Test

class PerceptualHasherTest {

    @Test
    fun `hamming distance should be 0 for same hash`() {
        val hash = 0b1010101010101010L
        assertEquals(0, PerceptualHasher.hammingDistance(hash, hash))
    }

    @Test
    fun `hamming distance should count different bits`() {
        val hash1 = 0b1010101010101010L
        val hash2 = 0b0101010101010101L
        assertEquals(16, PerceptualHasher.hammingDistance(hash1, hash2))
    }

    @Test
    fun `similarity should be 1 for same hash`() {
        val hash = 0b1010101010101010L
        assertEquals(1.0f, PerceptualHasher.similarity(hash, hash), 0.01f)
    }

    @Test
    fun `similarity should be 0 for completely different hash`() {
        val hash1 = 0L
        val hash2 = -1L  // all bits set
        assertEquals(0.0f, PerceptualHasher.similarity(hash1, hash2), 0.01f)
    }

    @Test
    fun `similarity should be between 0 and 1`() {
        val hash1 = 0b1111000011110000L
        val hash2 = 0b1111000000001111L
        val sim = PerceptualHasher.similarity(hash1, hash2)
        assertTrue(sim in 0.0f..1.0f)
    }
}
