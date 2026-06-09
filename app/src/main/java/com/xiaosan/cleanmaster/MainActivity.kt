package com.xiaosan.cleanmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.xiaosan.cleanmaster.navigation.AppNavigation
import com.xiaosan.cleanmaster.ui.theme.CleanMasterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CleanMasterTheme {
                AppNavigation()
            }
        }
    }
}
