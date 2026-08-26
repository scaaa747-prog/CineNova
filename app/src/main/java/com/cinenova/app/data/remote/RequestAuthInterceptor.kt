package com.cinenova.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that merges [RequestAuthProvider.headers] into every
 * outgoing request. Body-dependent providers see the request body as a
 * UTF-8 string (peeked, not consumed).
 */
class RequestAuthInterceptor(
    private val provider: RequestAuthProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val bodyString: String? = request.body?.let { body ->
            okio.Buffer().apply { body.writeTo(this) }.readUtf8()
        }

        val extraHeaders = provider.headers(
            method = request.method,
            url = request.url.toString(),
            requestBody = bodyString,
        )

        if (extraHeaders.isEmpty()) return chain.proceed(request)

        val builder = request.newBuilder()
        extraHeaders.forEach { (name, value) -> builder.header(name, value) }
        return chain.proceed(builder.build())
    }
}
