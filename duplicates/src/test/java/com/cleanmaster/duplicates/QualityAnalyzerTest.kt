package com.cleanmaster.duplicates

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class QualityAnalyzerTest {

    @Test
    fun `should detect black screen`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLACK)
        assertTrue(QualityAnalyzer.isBlackScreen(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `should not detect normal image as black screen`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        assertFalse(QualityAnalyzer.isBlackScreen(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `should detect solid color`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLUE)
        assertTrue(QualityAnalyzer.isSolidColor(bitmap))
        bitmap.recycle()
    }

    @Test
    fun `blur score should be low for uniform image`() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        val score = QualityAnalyzer.blurScore(bitmap)
        assertTrue(score < 0.1f)
        bitmap.recycle()
    }
}
