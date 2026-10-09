package com.example.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "SamadApiClient"
    private const val USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/153.0.0.0 Safari/537.36"

    /**
     * Provider lambda to supply active bearer token dynamically
     */
    var tokenProvider: (() -> String?)? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Full logging of requests and responses for debugging
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, message)
        // Also print to stderr/stdout for local test execution logs
        println("[HTTP] $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor: Adds User-Agent, Origin, Referer, Accept, and Bearer token (except login)
    private val authAndHeadersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .header("Origin", "https://samad.app")
            .header("Referer", "https://samad.app/")

        val urlPath = original.url.encodedPath

        // If not login request, ensure Bearer token is attached
        if (!urlPath.contains("oauth/token")) {
            val currentAuth = original.header("Authorization")
            if (currentAuth.isNullOrBlank()) {
                val token = tokenProvider?.invoke()
                if (!token.isNullOrBlank()) {
                    val bearerToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                    requestBuilder.header("Authorization", bearerToken)
                }
            } else if (!currentAuth.startsWith("Bearer ") && !currentAuth.startsWith("Basic ")) {
                requestBuilder.header("Authorization", "Bearer $currentAuth")
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authAndHeadersInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun createService(baseUrl: String, tokenProvider: (() -> String?)? = null): SamadApiService {
        if (tokenProvider != null) {
            this.tokenProvider = tokenProvider
        }
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SamadApiService::class.java)
    }
}
