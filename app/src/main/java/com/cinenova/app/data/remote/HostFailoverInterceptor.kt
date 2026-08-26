package com.cinenova.app.data.remote

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Host failover: attempts [ApiConfig.PRIMARY_HOST] first, then each fallback
 * host in order. Moves to the next host on IOException or HTTP 5xx.
 * Non-retryable responses (4xx etc.) are returned as-is.
 */
class HostFailoverInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentHost = original.url.host

        // If a previous attempt already rewrote the host, resume from there.
        val startIndex = ApiConfig.ALL_HOSTS.indexOfFirst { it == currentHost }.let {
            if (it >= 0) it else 0
        }

        var lastResponse: Response? = null
        var lastError: IOException? = null

        for (index in startIndex until ApiConfig.ALL_HOSTS.size) {
            val targetUrl = original.url.newBuilder()
                .scheme("https")
                .host(ApiConfig.ALL_HOSTS[index])
                .build()
            val rewritten = original.newBuilder().url(targetUrl).build()

            try {
                lastResponse?.close()
                val response = chain.proceed(rewritten)
                lastResponse = response
                if (response.code < 500) return response
            } catch (e: IOException) {
                lastError = e
            }
        }

        lastResponse?.let { return it }
        throw lastError ?: IOException("All upstream hosts failed for ${original.url.encodedPath}")
    }
}
