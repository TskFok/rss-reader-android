package com.example.rssreader.core.network

import com.example.rssreader.core.auth.TokenProvider
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}

class DynamicBaseUrlInterceptor(
    private val baseUrlProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val base = baseUrlProvider().trimEnd('/').toHttpUrlOrNull() ?: return chain.proceed(chain.request())
        val oldUrl = chain.request().url
        val mergedPath = base.encodedPath.trimEnd('/') + "/" + oldUrl.encodedPath.trimStart('/')

        val newUrl = oldUrl.newBuilder()
            .scheme(base.scheme)
            .host(base.host)
            .port(base.port)
            .encodedPath(mergedPath)
            .build()

        return chain.proceed(chain.request().newBuilder().url(newUrl).build())
    }
}
