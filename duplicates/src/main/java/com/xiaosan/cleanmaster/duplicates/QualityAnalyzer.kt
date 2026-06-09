package com.xiaosan.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color

object QualityAnalyzer {

    private const val BLACK_THRESHOLD = 30
    private const val SOLID_COLOR_THRESHOLD = 0.95

    /**
     * Detects if an image is mostly black (average brightness < threshold).
     */
    fun isBlackScreen(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
        var totalBrightness = 0L
        var pixelCount = 0

        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                val pixel = scaled.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
                totalBrightness += brightness
                pixelCount++
            }
        }

        val avgBrightness = totalBrightness / pixelCount
        scaled.recycle()
        return avgBrightness < BLACK_THRESHOLD
    }

    /**
     * Detects if an image is a solid color (dominant color > 95% of pixels).
     */
    fun isSolidColor(bitmap: Bitmap): Boolean {
        val scaled = Bitmap.createScaledBitmap(bitmap, 50, 50, true)
        val colorCounts = mutableMapOf<Int, Int>()
        var totalPixels = 0

        for (y in 0 until scaled.height) {
            for (x in 0 until scaled.width) {
                // Quantize to reduce color space
                val pixel = scaled.getPixel(x, y)
                val quantized = Color.rgb(
                    Color.red(pixel) / 16 * 16,
                    Color.green(pixel) / 16 * 16,
                    Color.blue(pixel) / 16 * 16
                )
                colorCounts[quantized] = (colorCounts[quantized] ?: 0) + 1
                totalPixels++
            }
        }

        scaled.recycle()
        val maxCount = colorCounts.values.maxOrNull() ?: 0
        return maxCount.toFloat() / totalPixels > SOLID_COLOR_THRESHOLD
    }

    /**
     * Calculates blur score using Laplacian variance.
     * Lower score = more blurry. 0 = completely uniform.
     */
    fun blurScore(bitmap: Bitmap): Float {
        val scaled = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
        val pixels = Array(scaled.height) { y ->
            IntArray(scaled.width) { x ->
                val pixel = scaled.getPixel(x, y)
                (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
            }
        }

        scaled.recycle()

        // Laplacian kernel: [0,1,0; 1,-4,1; 0,1,0]
        var sum = 0.0
        var count = 0
        for (y in 1 until pixels.size - 1) {
            for (x in 1 until pixels[0].size - 1) {
                val laplacian = pixels[y - 1][x] + pixels[y + 1][x] +
                    pixels[y][x - 1] + pixels[y][x + 1] - 4 * pixels[y][x]
                sum += laplacian * laplacian
                count++
            }
        }

        return (sum / count).toFloat()
    }
}
