package com.example.rssreader.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val dynamicColor: Boolean = true,
    val status: UiState<Unit>? = null,
)

class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            _uiState.update { it.copy(baseUrl = settings.baseUrl, dynamicColor = settings.dynamicColorEnabled) }
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            container.settingsRepository.updateDynamicColor(enabled)
            _uiState.update { it.copy(dynamicColor = enabled) }
        }
    }

    fun logout(onFinished: () -> Unit) {
        viewModelScope.launch {
            runCatching {
                container.authRepository.logout()
                container.clearUserData()
            }.onSuccess {
                onFinished()
            }.onFailure { e ->
                _uiState.update { it.copy(status = UiState.Error(e.message ?: "退出失败")) }
            }
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(status = null) }
    }
}
