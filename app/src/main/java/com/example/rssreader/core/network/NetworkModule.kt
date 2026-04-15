package com.example.rssreader.core.network

import com.example.rssreader.core.auth.SessionStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class NetworkModule(
    sessionStore: SessionStore,
    baseUrlProvider: () -> String,
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(DynamicBaseUrlInterceptor(baseUrlProvider))
        .addInterceptor(AuthInterceptor(sessionStore))
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://placeholder.local/")
        .client(okHttp)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val feedsApi: FeedsApi = retrofit.create(FeedsApi::class.java)
    val articlesApi: ArticlesApi = retrofit.create(ArticlesApi::class.java)
    val summaryApi: SummaryApi = retrofit.create(SummaryApi::class.java)
}
