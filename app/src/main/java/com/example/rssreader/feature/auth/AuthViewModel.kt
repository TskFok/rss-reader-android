package com.example.rssreader.feature.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.datastore.AppSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val baseUrl: String = AppSettingsStore.DEFAULT_BASE_URL,
    val baseUrlSaved: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            _uiState.update {
                it.copy(
                    baseUrl = settings.baseUrl,
                    baseUrlSaved = settings.baseUrl != AppSettingsStore.DEFAULT_BASE_URL,
                    isLoggedIn = container.authRepository.hasSession(),
                )
            }
        }
    }

    fun saveBaseUrl(value: String) {
        if (!AppSettingsStore.isValidBaseUrl(value)) {
            _uiState.update { it.copy(error = "服务端地址必须以 http:// 或 https:// 开头") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                container.updateBaseUrl(value)
                container.authRepository.logout()
                container.clearUserData()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        baseUrl = AppSettingsStore.normalizeBaseUrl(value),
                        baseUrlSaved = true,
                        isLoading = false,
                        isLoggedIn = false,
                        error = null,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "保存地址失败") }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                container.authRepository.login(username, password)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "登录失败") }
            }
        }
    }

    fun beginFeishuLogin(onLaunch: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { container.authRepository.getFeishuLoginUrl() }
                .onSuccess { gotoUrl ->
                    _uiState.update { it.copy(isLoading = false) }
                    onLaunch(gotoUrl)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "获取飞书登录地址失败") }
                }
        }
    }

    fun handleFeishuCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code.isNullOrBlank()) {
            _uiState.update { it.copy(error = "飞书回调缺少 code") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                container.authRepository.exchangeFeishuCode(code, state)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "飞书登录失败") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
