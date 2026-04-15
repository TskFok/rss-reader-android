package com.example.rssreader.feature.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rssreader.app.AppContainer
import com.example.rssreader.core.model.Article
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

enum class ArticleReadFilter(val label: String) {
    ALL("全部"),
    READ("已读"),
    UNREAD("未读"),
}

class ArticlesViewModel(
    private val container: AppContainer,
    private val feedId: Long,
) : ViewModel() {
    private val filterState = MutableStateFlow(ArticleReadFilter.UNREAD)
    private val refreshState = MutableStateFlow(0)
    val readFilter: StateFlow<ArticleReadFilter> = filterState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingFlow: Flow<PagingData<Article>> = combine(filterState, refreshState) { filter, _ -> filter }
        .flatMapLatest { filter ->
            container.articleRepository.pager(
                feedId = feedId,
                read = when (filter) {
                    ArticleReadFilter.ALL -> null
                    ArticleReadFilter.READ -> true
                    ArticleReadFilter.UNREAD -> false
                },
            )
        }
        .cachedIn(viewModelScope)

    private val markedReadIds = mutableSetOf<Long>()

    fun setReadFilter(filter: ArticleReadFilter) {
        if (filterState.value == filter) return
        filterState.value = filter
    }

    fun markAsReadOnOpen(article: Article) {
        if (article.read || markedReadIds.contains(article.id)) return
        viewModelScope.launch {
            runCatching { container.articleRepository.markAsRead(article.id) }
                .onSuccess {
                    markedReadIds += article.id
                    if (filterState.value == ArticleReadFilter.UNREAD) {
                        refreshState.value = refreshState.value + 1
                    }
                }
        }
    }
}
