package com.example.rssreader.app

import android.content.Context
import com.example.rssreader.core.auth.SessionStore
import com.example.rssreader.core.database.AppDatabase
import com.example.rssreader.core.datastore.AppSettingsStore
import com.example.rssreader.core.network.ArticleRepository
import com.example.rssreader.core.network.AuthRepository
import com.example.rssreader.core.network.FeedRepository
import com.example.rssreader.core.network.NetworkModule
import com.example.rssreader.core.network.SettingsRepository
import com.example.rssreader.core.network.SummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val sessionStore = SessionStore(appContext)
    val settingsStore = AppSettingsStore(appContext)
    private val db = AppDatabase.create(appContext)

    private val baseUrlState = MutableStateFlow(AppSettingsStore.DEFAULT_BASE_URL)

    init {
        runBlocking {
            baseUrlState.value = settingsStore.settingsFlow.first().baseUrl
        }
    }

    val settingsRepository = SettingsRepository(settingsStore)

    val networkModule = NetworkModule(
        sessionStore = sessionStore,
        baseUrlProvider = { baseUrlState.value },
    )

    val authRepository = AuthRepository(networkModule.authApi, sessionStore)
    val feedRepository = FeedRepository(networkModule.feedsApi, db.feedDao())
    val articleRepository = ArticleRepository(networkModule.articlesApi)
    val summaryRepository = SummaryRepository(networkModule.summaryApi)

    suspend fun updateBaseUrl(url: String) {
        settingsRepository.updateBaseUrl(url)
        baseUrlState.value = AppSettingsStore.normalizeBaseUrl(url)
    }

    suspend fun clearUserData() {
        feedRepository.clear()
    }
}
