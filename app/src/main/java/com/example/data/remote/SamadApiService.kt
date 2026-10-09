package com.example.data.remote

import com.example.data.model.ForgotCodeResponse
import com.example.data.model.ReservesResponse
import com.example.data.model.TokenResponse
import com.example.data.model.UserInfoResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SamadApiService {

    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun login(
        @Header("Authorization") auth: String = "Basic c2FtYWQtbW9iaWxlOnNhbWFkLW1vYmlsZS1zZWNyZXQ=",
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
        @Field("scope") scope: String = "read+write"
    ): TokenResponse

    @GET("rest/users/me")
    suspend fun getUserInfo(
        @Header("Authorization") bearerToken: String = ""
    ): UserInfoResponse

    @GET("rest/reserves")
    suspend fun getReserves(
        @Header("Authorization") bearer: String,
        @Query(value = "weekStartDate", encoded = true) weekStart: String,
        @Query("selfType") selfType: String = "NORMAL"
    ): ReservesResponse

    @GET("rest/forget-card-codes/print")
    suspend fun getForgotCardCode(
        @Header("Authorization") bearer: String,
        @Query("reserveId") reserveId: Long,
        @Query("count") count: Int = 1,
        @Query("dailySale") dailySale: Boolean = false
    ): ForgotCodeResponse
}
