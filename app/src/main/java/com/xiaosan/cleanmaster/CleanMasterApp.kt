package com.xiaosan.cleanmaster

import android.app.Application
import android.content.Context
import com.xiaosan.cleanmaster.util.LanguageManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CleanMasterApp : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(base))
    }
}
