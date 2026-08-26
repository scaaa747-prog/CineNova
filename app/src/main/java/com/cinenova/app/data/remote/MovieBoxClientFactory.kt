package com.cinenova.app.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds the resilient OkHttp client + Retrofit service:
 *
 *   HostFailoverInterceptor -> BearerAuthInterceptor -> RequestAuthInterceptor -> ResponseTokenCaptureInterceptor
 *
 * Configured with generous timeouts (30s connect, 45s read) and automatic connection retry for low network coverage.
 */
object MovieBoxClientFactory {

    fun createOkHttp(authProvider: RequestAuthProvider): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HostFailoverInterceptor())
            .addInterceptor(BearerAuthInterceptor())
            .addInterceptor(RequestAuthInterceptor(authProvider))
            .addInterceptor(ResponseTokenCaptureInterceptor())
            .build()

    fun createApi(okHttpClient: OkHttpClient): MovieBoxApi =
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MovieBoxApi::class.java)

    fun create(authProvider: RequestAuthProvider): MovieBoxApi =
        createApi(createOkHttp(authProvider))
}
