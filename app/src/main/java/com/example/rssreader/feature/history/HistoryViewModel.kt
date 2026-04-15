package com.example.rssreader.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.model.SummaryHistoryItem
import kotlinx.coroutines.flow.Flow

class HistoryViewModel(
    private val container: AppContainer,
) : ViewModel() {
    val pagingFlow: Flow<PagingData<SummaryHistoryItem>> = container.summaryRepository.pager().cachedIn(viewModelScope)
}
