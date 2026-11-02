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
    private val USER_AGENT = "SamadFoodQR/${com.example.BuildConfig.VERSION_NAME} (Android)"

    /**
     * Provider lambda to supply active bearer token dynamically
     */
    var tokenProvider: (() -> String?)? = null

    /**
     * Provider lambda to re-authenticate and return a fresh bearer token on HTTP 401
     */
    var reLoginProvider: (() -> String?)? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Secure logging: only in DEBUG, redact Authorization and passwords, no println
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        if (com.example.BuildConfig.DEBUG) {
            val sanitized = if (message.contains("password", ignoreCase = true)) {
                message
                    .replace(Regex("""("password"\s*:\s*")[^"]+(")"""), """$1[REDACTED]$2""")
                    .replace(Regex("""(password=[^& ]+)"""), "password=[REDACTED]")
            } else {
                message
            }
            Log.d(TAG, sanitized)
        }
    }.apply {
        redactHeader("Authorization")
        level = if (com.example.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // Interceptor: Adds honest User-Agent and Bearer token (except login)
    private val authAndHeadersInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")

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

    private val tokenAuthenticator = okhttp3.Authenticator { _, response ->
        val path = response.request.url.encodedPath
        // Do not attempt re-login if the failing request was oauth/token itself
        if (path.contains("oauth/token")) {
            return@Authenticator null
        }

        // Avoid infinite retry loops: max 2 prior responses
        var prior = response.priorResponse
        var retryCount = 0
        while (prior != null) {
            retryCount++
            prior = prior.priorResponse
        }
        if (retryCount >= 2) {
            if (com.example.BuildConfig.DEBUG) {
                Log.w(TAG, "Authenticator: Giving up after $retryCount 401 retries on $path")
            }
            return@Authenticator null
        }

        val newToken = reLoginProvider?.invoke()
        if (newToken.isNullOrBlank()) {
            return@Authenticator null
        }

        val bearerToken = if (newToken.startsWith("Bearer ")) newToken else "Bearer $newToken"
        response.request.newBuilder()
            .header("Authorization", bearerToken)
            .build()
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authAndHeadersInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun createService(
        baseUrl: String,
        tokenProvider: (() -> String?)? = null,
        reLoginProvider: (() -> String?)? = null
    ): SamadApiService {
        if (tokenProvider != null) {
            this.tokenProvider = tokenProvider
        }
        if (reLoginProvider != null) {
            this.reLoginProvider = reLoginProvider
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
