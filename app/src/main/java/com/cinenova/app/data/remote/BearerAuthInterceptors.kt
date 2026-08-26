package com.cinenova.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/**
 * Captures the JWT from the `x-user` response header emitted during the
 * bootstrap call and stores it in [JwtTokenStore]. Purely passive: it reads
 * what the server returns; it does not generate or sign anything.
 */
class ResponseTokenCaptureInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val xUser = response.header(HEADER_X_USER)
        if (!xUser.isNullOrBlank()) {
            runCatching {
                JSONObject(xUser).optString("token").takeIf { it.isNotBlank() }
            }.getOrNull()?.let { JwtTokenStore.update(it) }
        }

        return response
    }

    private companion object {
        const val HEADER_X_USER = "x-user"
    }
}

/**
 * Replays the stored bootstrap JWT as `Authorization: Bearer <token>` on all
 * subsequent requests until a new token arrives.
 */
class BearerAuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = JwtTokenStore.current() ?: return chain.proceed(chain.request())
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
