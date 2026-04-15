package com.example.rssreader.core.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rssreader.core.model.Article
import com.example.rssreader.core.model.SummaryHistoryItem

class ArticlesPagingSource(
    private val api: ArticlesApi,
    private val feedId: Long,
    private val read: Boolean?,
) : PagingSource<Int, Article>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        return try {
            val page = params.key ?: 1
            val size = params.loadSize.coerceAtMost(50)
            val response = api.getArticles(feedId = feedId, read = read, page = page, pageSize = size)
            val next = if (response.items.isEmpty()) null else page + 1
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = next,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}

class SummaryPagingSource(
    private val api: SummaryApi,
) : PagingSource<Int, SummaryHistoryItem>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SummaryHistoryItem> {
        return try {
            val page = params.key ?: 1
            val size = params.loadSize.coerceAtMost(50)
            val response = api.getSummaryHistories(page = page, pageSize = size)
            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.items.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SummaryHistoryItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
