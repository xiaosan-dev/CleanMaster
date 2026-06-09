package com.cleanmaster.ad

import android.app.Activity
import android.content.Context

/**
 * CSJ (穿山甲) ad provider placeholder.
 * Actual implementation requires CSJ SDK integration.
 */
class CSJProvider(private val context: Context) {

    fun initialize(appId: String) {
        // CSJ SDK initialization
    }

    fun loadInterstitial(adUnitId: String) {
        // Load CSJ interstitial ad
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        // Show CSJ interstitial ad
        onDismissed()
    }

    fun isInterstitialReady(): Boolean = false
}
