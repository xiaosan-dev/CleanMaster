package com.cleanmaster.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class AppLanguage(val displayName: String, val code: String) {
    SYSTEM("跟随系统", "system"),
    ENGLISH("English", "en"),
    CHINESE("中文", "zh")
}

data class SettingsUiState(
    val selectedLanguage: AppLanguage = AppLanguage.SYSTEM,
    val trashCapacityMB: Int = 2048,
    val expiredDays: Int = 30
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val langCode = prefs.getString("language", "system") ?: "system"
        val language = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.SYSTEM

        _uiState.update {
            it.copy(
                selectedLanguage = language,
                trashCapacityMB = prefs.getInt("trash_capacity_mb", 2048),
                expiredDays = prefs.getInt("expired_days", 30)
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.code).apply()
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun setTrashCapacity(mb: Int) {
        prefs.edit().putInt("trash_capacity_mb", mb).apply()
        _uiState.update { it.copy(trashCapacityMB = mb) }
    }

    fun setExpiredDays(days: Int) {
        prefs.edit().putInt("expired_days", days).apply()
        _uiState.update { it.copy(expiredDays = days) }
    }
}
