package com.example.rssreader.feature.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.model.Feed
import com.example.rssreader.core.model.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Feed>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Feed>>> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.feedRepository.observeFeeds().collectLatest { feeds ->
                _uiState.value = UiState.Success(feeds)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { container.feedRepository.refreshFeeds() }
                .onFailure { e -> _uiState.update { UiState.Error(e.message ?: "加载订阅失败") } }
        }
    }
}
