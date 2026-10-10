package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForgotCodeResponse(
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: Int? = 0,
    @Json(name = "message") val message: String? = null,
    @Json(name = "payload") val payload: ForgotCodePayload? = null
) : java.io.Serializable

@JsonClass(generateAdapter = true)
data class ForgotCodePayload(
    @Json(name = "username") val username: String? = "",
    @Json(name = "self") val self: String? = "",
    @Json(name = "meal") val meal: String? = "",
    @Json(name = "foodName") val foodName: String? = "",
    @Json(name = "foodType") val foodType: String? = null,
    @Json(name = "count") val count: Int = 1,
    @Json(name = "remainCount") val remainCount: Int? = 1,
    @Json(name = "valid") val valid: Boolean = true,
    @Json(name = "forgotCardCode") val forgotCardCode: String? = null
) : java.io.Serializable
