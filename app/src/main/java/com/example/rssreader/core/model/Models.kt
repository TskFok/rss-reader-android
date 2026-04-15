package com.example.rssreader.core.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
)

@JsonClass(generateAdapter = true)
data class LoginResult(
    val token: String,
    val user: User,
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String,
)

@JsonClass(generateAdapter = true)
data class User(
    val id: Long,
    val username: String,
    val status: String,
    @Json(name = "is_super_admin") val isSuperAdmin: Boolean,
    @Json(name = "feishu_id") val feishuId: String?,
    @Json(name = "feishu_name") val feishuName: String?,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class FeishuLoginUrlResponse(
    val url: String?,
    val goto: String,
)

@JsonClass(generateAdapter = true)
data class FeishuExchangeRequest(
    val code: String,
    val state: String? = null,
)

@JsonClass(generateAdapter = true)
data class Feed(
    val id: Long,
    @Json(name = "user_id") val userId: Long,
    @Json(name = "category_id") val categoryId: Long?,
    val url: String,
    val title: String,
    @Json(name = "last_fetched_at") val lastFetchedAt: String?,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class Article(
    val id: Long,
    @Json(name = "feed_id") val feedId: Long,
    val guid: String,
    val title: String,
    val link: String,
    val content: String,
    @Json(name = "ai_category") val aiCategory: String?,
    @Json(name = "title_translated") val titleTranslated: String?,
    @Json(name = "content_translated") val contentTranslated: String?,
    @Json(name = "published_at") val publishedAt: String?,
    @Json(name = "created_at") val createdAt: String,
    val read: Boolean,
    val favorite: Boolean,
    @Json(name = "feed_title") val feedTitle: String,
)

@JsonClass(generateAdapter = true)
data class SummaryHistoryItem(
    val id: Long,
    @Json(name = "ai_model_name") val aiModelName: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "article_count") val articleCount: Int,
    val total: Long,
    val content: String?,
    val error: String?,
    @Json(name = "created_at") val createdAt: String,
)

@JsonClass(generateAdapter = true)
data class PagedResponse<T>(
    val items: List<T>,
    val total: Long,
)

@JsonClass(generateAdapter = true)
data class ErrorBody(
    val error: String,
)
