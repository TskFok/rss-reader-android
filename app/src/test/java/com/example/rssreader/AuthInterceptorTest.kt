package com.example.rssreader

import com.example.rssreader.core.auth.TokenProvider
import com.example.rssreader.core.network.AuthInterceptor
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun addsBearerTokenHeaderWhenTokenExists() {
        val interceptor = AuthInterceptor(tokenProvider = TokenProvider { "abc" })
        val chain = FakeChain(Request.Builder().url("https://example.com/feeds").build())

        interceptor.intercept(chain)

        assertEquals("Bearer abc", chain.capturedRequest.header("Authorization"))
    }

    @Test
    fun skipsHeaderWhenTokenMissing() {
        val interceptor = AuthInterceptor(tokenProvider = TokenProvider { null })
        val chain = FakeChain(Request.Builder().url("https://example.com/feeds").build())

        interceptor.intercept(chain)

        assertNull(chain.capturedRequest.header("Authorization"))
    }

    private class FakeChain(private val request: Request) : Interceptor.Chain {
        lateinit var capturedRequest: Request

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            capturedRequest = request
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("ok".toResponseBody())
                .build()
        }

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit): Interceptor.Chain = this
    }
}
