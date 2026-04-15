package com.example.rssreader.core.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.rssreader.core.auth.SessionStore
import com.example.rssreader.core.database.FeedDao
import com.example.rssreader.core.database.FeedEntity
import com.example.rssreader.core.datastore.AppSettingsStore
import com.example.rssreader.core.model.Article
import com.example.rssreader.core.model.FeishuExchangeRequest
import com.example.rssreader.core.model.Feed
import com.example.rssreader.core.model.LoginRequest
import com.example.rssreader.core.model.LoginResult
import com.example.rssreader.core.model.SummaryHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) {
    suspend fun login(username: String, password: String): LoginResult {
        val result = authApi.login(LoginRequest(username = username, password = password))
        sessionStore.saveSession(result.token, result.user)
        return result
    }

    suspend fun getFeishuLoginUrl(): String = authApi.getFeishuLoginUrl().goto

    suspend fun exchangeFeishuCode(code: String, state: String?): LoginResult {
        val result = authApi.exchangeFeishuCode(FeishuExchangeRequest(code = code, state = state))
        sessionStore.saveSession(result.token, result.user)
        return result
    }

    fun hasSession(): Boolean = !sessionStore.getToken().isNullOrBlank()

    fun logout() {
        sessionStore.clear()
    }
}

class SettingsRepository(
    private val settingsStore: AppSettingsStore,
) {
    val settingsFlow = settingsStore.settingsFlow

    suspend fun updateBaseUrl(url: String) = settingsStore.updateBaseUrl(url)

    suspend fun updateDynamicColor(enabled: Boolean) = settingsStore.updateDynamicColor(enabled)

    suspend fun currentBaseUrl(): String = settingsStore.settingsFlow.first().baseUrl
}

class FeedRepository(
    private val feedsApi: FeedsApi,
    private val dao: FeedDao,
) {
    fun observeFeeds(): Flow<List<Feed>> = dao.observeFeeds().map { list -> list.map { it.toModel() } }

    suspend fun refreshFeeds() {
        val remote = feedsApi.getFeeds()
        dao.clearAll()
        dao.insertAll(remote.map(FeedEntity.Companion::fromModel))
    }

    suspend fun clear() = dao.clearAll()
}

class ArticleRepository(
    private val api: ArticlesApi,
) {
    fun pager(feedId: Long, read: Boolean? = null): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 3, enablePlaceholders = false),
            pagingSourceFactory = { ArticlesPagingSource(api = api, feedId = feedId, read = read) },
        ).flow
    }
}

class SummaryRepository(
    private val api: SummaryApi,
) {
    fun pager(): Flow<PagingData<SummaryHistoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 3, enablePlaceholders = false),
            pagingSourceFactory = { SummaryPagingSource(api) },
        ).flow
    }
}
