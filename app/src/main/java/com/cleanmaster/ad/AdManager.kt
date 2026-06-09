package com.cleanmaster.ad

import android.app.Activity
import com.google.android.gms.ads.AdView

enum class AdTrigger {
    SCAN_COMPLETE,
    CLEAN_COMPLETE,
    RESTORE_COMPLETE,
    TRASH_EMPTIED
}

class AdManager(
    private val admobProvider: AdMobProvider,
    private val csjProvider: CSJProvider
) {
    private var lastInterstitialTime = mutableMapOf<AdTrigger, Long>()
    private val cooldownMs = 5 * 60 * 1000L // 5 minutes

    fun initialize() {
        admobProvider.initialize()
    }

    fun loadBanner(adView: AdView) {
        admobProvider.loadBanner(adView)
    }

    fun preloadInterstitial() {
        admobProvider.loadInterstitial("ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx")
    }

    fun showInterstitialIfReady(activity: Activity, trigger: AdTrigger, onDismissed: () -> Unit) {
        val lastTime = lastInterstitialTime[trigger] ?: 0
        val now = System.currentTimeMillis()

        if (now - lastTime < cooldownMs) {
            onDismissed()
            return
        }

        if (admobProvider.isInterstitialReady()) {
            lastInterstitialTime[trigger] = now
            admobProvider.showInterstitial(activity, onDismissed)
        } else if (csjProvider.isInterstitialReady()) {
            lastInterstitialTime[trigger] = now
            csjProvider.showInterstitial(activity, onDismissed)
        } else {
            onDismissed()
        }
    }

    fun showSplashAd(activity: Activity, onDismissed: () -> Unit) {
        admobProvider.showInterstitial(activity, onDismissed)
    }
}
