package com.cleanmaster.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cleanmaster.R
import com.cleanmaster.core.util.FileSizeFormatter

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        StorageOverviewCard(
            totalStorage = uiState.totalStorage,
            usedStorage = uiState.usedStorage
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isScanning) {
            ScanProgressCard(progress = uiState.scanProgress)
        } else if (uiState.scanResult != null) {
            ScanResultCard(result = uiState.scanResult!!)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startScan() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !uiState.isScanning,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (uiState.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.CleaningServices, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uiState.isScanning) stringResource(R.string.scan_button) + "..." else stringResource(R.string.scan_button),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun StorageOverviewCard(totalStorage: Long, usedStorage: Long) {
    val progress = if (totalStorage > 0) usedStorage.toFloat() / totalStorage else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.storage_used, FileSizeFormatter.format(usedStorage)),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.storage_available, FileSizeFormatter.format(totalStorage - usedStorage)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScanProgressCard(progress: com.cleanmaster.scanner.ScanProgress?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.scan_button) + "...", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            progress?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${it.scannedCount} files scanned", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ScanResultCard(result: com.cleanmaster.core.model.ScanResult) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.cleanable_size, FileSizeFormatter.format(result.totalSize)),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (result.cacheFiles.isNotEmpty()) {
                ResultRow(stringResource(R.string.cache_files), result.cacheFiles.size, result.cacheFiles.sumOf { it.size })
            }
            if (result.junkFiles.isNotEmpty()) {
                ResultRow(stringResource(R.string.junk_files), result.junkFiles.size, result.junkFiles.sumOf { it.size })
            }
            if (result.emptyFolders.isNotEmpty()) {
                ResultRow(stringResource(R.string.empty_folders), result.emptyFolders.size, 0)
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, count: Int, size: Long) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "$count items${if (size > 0) " · ${FileSizeFormatter.format(size)}" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
