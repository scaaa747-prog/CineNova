package com.cinenova.app.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builds the OkHttp client + Retrofit service with the interceptor chain:
 *
 *     RequestAuthInterceptor → HostFailoverInterceptor → network
 *
 * (Auth runs first so signature providers observe the final URL.)
 */
object MovieBoxClientFactory {

    fun createOkHttp(authProvider: RequestAuthProvider): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(RequestAuthInterceptor(authProvider))
            .addInterceptor(HostFailoverInterceptor())
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
