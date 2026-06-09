package com.cleanmaster.ui.home

import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cleanmaster.core.model.ScanResult
import com.cleanmaster.scanner.FileScanner
import com.cleanmaster.scanner.ScanProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isScanning: Boolean = false,
    val scanProgress: ScanProgress? = null,
    val scanResult: ScanResult? = null,
    val totalStorage: Long = 0,
    val usedStorage: Long = 0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val fileScanner = FileScanner()

    init {
        loadStorageInfo()
    }

    private fun loadStorageInfo() {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes
        val available = stat.availableBytes
        _uiState.update {
            it.copy(totalStorage = total, usedStorage = total - available)
        }
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }

            try {
                val rootPath = Environment.getExternalStorageDirectory().absolutePath
                fileScanner.scan(rootPath).collect { progress ->
                    _uiState.update { it.copy(scanProgress = progress) }
                }

                val result = fileScanner.getResult()
                _uiState.update {
                    it.copy(isScanning = false, scanResult = result, scanProgress = null)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isScanning = false, error = e.message)
                }
            }
        }
    }
}
