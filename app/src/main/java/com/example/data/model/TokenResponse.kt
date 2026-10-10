package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String? = null,
    @Json(name = "expires_in") val expiresIn: Long = 0L,
    @Json(name = "token_type") val tokenType: String = "bearer",
    @Json(name = "scope") val scope: String? = null,
    @Json(name = "jti") val jti: String? = null
)
