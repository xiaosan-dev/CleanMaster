package com.cleanmaster.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cleanmaster.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingCard(title = stringResource(R.string.language_setting)) {
            LanguageSelector(
                selected = uiState.selectedLanguage,
                onSelect = { viewModel.setLanguage(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard(title = stringResource(R.string.trash_capacity)) {
            TrashCapacitySelector(
                capacityMB = uiState.trashCapacityMB,
                onChange = { viewModel.setTrashCapacity(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard(title = stringResource(R.string.expired_days)) {
            ExpiredDaysSelector(
                days = uiState.expiredDays,
                onChange = { viewModel.setExpiredDays(it) }
            )
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LanguageSelector(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    Column {
        AppLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == language, onClick = { onSelect(language) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = language.displayName)
            }
        }
    }
}

@Composable
private fun TrashCapacitySelector(capacityMB: Int, onChange: (Int) -> Unit) {
    val options = listOf(1024, 2048, 5120, 10240, 0)
    var selectedIndex by remember { mutableIntStateOf(options.indexOf(capacityMB).coerceAtLeast(1)) }

    Column {
        Text(
            text = if (capacityMB == 0) "Unlimited" else "${capacityMB / 1024} GB",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { selectedIndex = it.toInt(); onChange(options[selectedIndex]) },
            valueRange = 0f..(options.size - 1).toFloat(),
            steps = options.size - 2
        )
    }
}

@Composable
private fun ExpiredDaysSelector(days: Int, onChange: (Int) -> Unit) {
    val options = listOf(7, 14, 30, 60, 90)
    var selectedIndex by remember { mutableIntStateOf(options.indexOf(days).coerceAtLeast(2)) }

    Column {
        Text(text = "$days days", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { selectedIndex = it.toInt(); onChange(options[selectedIndex]) },
            valueRange = 0f..(options.size - 1).toFloat(),
            steps = options.size - 2
        )
    }
}
