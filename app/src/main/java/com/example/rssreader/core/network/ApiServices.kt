package com.example.rssreader.core.network

import com.example.rssreader.core.model.Article
import com.example.rssreader.core.model.Feed
import com.example.rssreader.core.model.FeishuExchangeRequest
import com.example.rssreader.core.model.FeishuLoginUrlResponse
import com.example.rssreader.core.model.LoginRequest
import com.example.rssreader.core.model.LoginResult
import com.example.rssreader.core.model.PagedResponse
import com.example.rssreader.core.model.SummaryHistoryItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResult

    @GET("auth/feishu/login-url")
    suspend fun getFeishuLoginUrl(): FeishuLoginUrlResponse

    // Temporary endpoint name for Feishu OAuth code exchange.
    @POST("auth/feishu/exchange")
    suspend fun exchangeFeishuCode(@Body body: FeishuExchangeRequest): LoginResult
}

interface FeedsApi {
    @GET("feeds")
    suspend fun getFeeds(): List<Feed>
}

interface ArticlesApi {
    @GET("articles")
    suspend fun getArticles(
        @Query("feed_id") feedId: Long,
        @Query("read") read: Boolean? = null,
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): PagedResponse<Article>
}

interface SummaryApi {
    @GET("summary-histories")
    suspend fun getSummaryHistories(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
    ): PagedResponse<SummaryHistoryItem>
}
