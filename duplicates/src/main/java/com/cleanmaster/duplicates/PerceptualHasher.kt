package com.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.PI

object PerceptualHasher {

    private const val HASH_SIZE = 8
    private const val RESIZE_SIZE = 32

    /**
     * Calculates perceptual hash for a bitmap using DCT.
     * Returns a 64-bit hash as Long.
     */
    suspend fun calculatePHash(bitmap: Bitmap): Long = withContext(Dispatchers.Default) {
        // Step 1: Resize to 32x32
        val resized = Bitmap.createScaledBitmap(bitmap, RESIZE_SIZE, RESIZE_SIZE, true)

        // Step 2: Convert to grayscale
        val grayscale = Array(RESIZE_SIZE) { y ->
            IntArray(RESIZE_SIZE) { x ->
                val pixel = resized.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            }
        }

        // Step 3: Apply DCT
        val dct = applyDCT(grayscale)

        // Step 4: Extract top-left 8x8
        val lowFreq = Array(HASH_SIZE) { y ->
            DoubleArray(HASH_SIZE) { x -> dct[y][x] }
        }

        // Step 5: Calculate mean (excluding DC component)
        var sum = 0.0
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                if (y == 0 && x == 0) continue
                sum += lowFreq[y][x]
            }
        }
        val mean = sum / (HASH_SIZE * HASH_SIZE - 1)

        // Step 6: Generate hash
        var hash = 0L
        for (y in 0 until HASH_SIZE) {
            for (x in 0 until HASH_SIZE) {
                if (lowFreq[y][x] > mean) {
                    hash = hash or (1L shl (y * HASH_SIZE + x))
                }
            }
        }

        hash
    }

    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var xor = hash1 xor hash2
        var count = 0
        while (xor != 0L) {
            count++
            xor = xor and (xor - 1)
        }
        return count
    }

    fun similarity(hash1: Long, hash2: Long): Float {
        val distance = hammingDistance(hash1, hash2)
        return 1.0f - (distance.toFloat() / 64f)
    }

    private fun applyDCT(input: Array<IntArray>): Array<DoubleArray> {
        val n = input.size
        val result = Array(n) { DoubleArray(n) }

        for (u in 0 until n) {
            for (v in 0 until n) {
                var sum = 0.0
                for (i in 0 until n) {
                    for (j in 0 until n) {
                        sum += input[i][j] *
                            cos((2 * i + 1) * u * PI / (2 * n)) *
                            cos((2 * j + 1) * v * PI / (2 * n))
                    }
                }
                val cu = if (u == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
                result[u][v] = 0.25 * cu * cv * sum
            }
        }

        return result
    }
}
