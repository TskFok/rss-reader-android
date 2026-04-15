package com.example.rssreader.feature.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.model.Article
import kotlinx.coroutines.flow.Flow

class ArticlesViewModel(
    private val container: AppContainer,
    private val feedId: Long,
) : ViewModel() {
    val pagingFlow: Flow<PagingData<Article>> = container.articleRepository.pager(feedId).cachedIn(viewModelScope)
}
