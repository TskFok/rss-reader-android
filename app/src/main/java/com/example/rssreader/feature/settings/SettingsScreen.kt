package com.example.rssreader.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rssreader.core.model.UiState

@Composable
fun SettingsSheet(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.status) {
        val err = state.status as? UiState.Error ?: return@LaunchedEffect
        snackbar.showSnackbar(err.message)
        viewModel.clearStatus()
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SnackbarHost(snackbar)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("服务端地址", style = MaterialTheme.typography.labelMedium)
                Text(state.baseUrl, style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Material You 动态色", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = state.dynamicColor, onCheckedChange = viewModel::setDynamicColor)
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.logout(onLogout) }) {
            Text("退出登录")
        }
    }
}
