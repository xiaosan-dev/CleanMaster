package com.xiaosan.cleanmaster.ad

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdMobProvider(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null

    fun initialize() {
        MobileAds.initialize(context) {}
    }

    fun loadBanner(adView: AdView) {
        adView.loadAd(AdRequest.Builder().build())
    }

    fun loadInterstitial(adUnitId: String) {
        InterstitialAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismissed()
                }
            }
            ad.show(activity)
        } ?: onDismissed()
    }

    fun isInterstitialReady(): Boolean = interstitialAd != null
}
