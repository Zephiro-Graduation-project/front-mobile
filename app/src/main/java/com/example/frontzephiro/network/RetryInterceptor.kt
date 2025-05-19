// RetryInterceptor.kt
package com.example.frontzephiro.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(
    private val maxRetries: Int = 2,
    private val retryDelayMillis: Long = 500L
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        while (attempt <= maxRetries) {
            try {
                return chain.proceed(request)
            } catch (e: IOException) {
                lastException = e
                attempt++
                // simple back-off
                try {
                    Thread.sleep(retryDelayMillis * attempt)
                } catch (_: InterruptedException) { /* ignore */ }
            }
        }
        // All retries failed – rethrow the last exception
        throw lastException!!
    }
}
