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

    @GET("rest/users/nurture-profiles")
    suspend fun getNurtureProfile(
        @Header("Authorization") bearer: String = ""
    ): com.example.data.model.NurtureProfileResponse

    @GET("rest/reservations/selfs")
    suspend fun getAvailableSelfs(
        @Header("Authorization") bearer: String = ""
    ): com.example.data.model.SelfsResponse

    @GET("rest/programs/v2")
    suspend fun getReservePrograms(
        @Header("Authorization") bearer: String = "",
        @Query("selfId") selfId: Int,
        @Query(value = "weekStartDate", encoded = true) weekStart: String
    ): com.example.data.model.ReserveProgramResponse

    @retrofit2.http.PUT("rest/reserves/{programId}/reserve")
    suspend fun executeReserve(
        @Header("Authorization") bearer: String = "",
        @retrofit2.http.Path("programId") programId: Long,
        @retrofit2.http.Body body: com.example.data.model.ReserveRequestBody
    ): okhttp3.ResponseBody

    @retrofit2.http.POST("rest/reserves/{programId}/reserve")
    suspend fun executeReservePost(
        @Header("Authorization") bearer: String = "",
        @retrofit2.http.Path("programId") programId: Long,
        @retrofit2.http.Body body: com.example.data.model.ReserveRequestBody
    ): okhttp3.ResponseBody

    @retrofit2.http.POST("rest/reserves")
    suspend fun executeReserveDirectPost(
        @Header("Authorization") bearer: String = "",
        @retrofit2.http.Body body: com.example.data.model.ReserveDirectBody
    ): okhttp3.ResponseBody
}
