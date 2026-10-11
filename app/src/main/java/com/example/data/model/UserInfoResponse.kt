package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfoResponse(
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "payload") val payload: UserPayload? = null
)

@JsonClass(generateAdapter = true)
data class UserPayload(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "gender") val gender: String? = null,
    @Json(name = "enabled") val enabled: Boolean = true,
    @Json(name = "student") val student: StudentInfo? = null
)

@JsonClass(generateAdapter = true)
data class StudentInfo(
    @Json(name = "studentNumber") val studentNumber: String? = null,
    @Json(name = "semester") val semester: String? = null,
    @Json(name = "entranceType") val entranceType: String? = null,
    @Json(name = "studyLevel") val studyLevel: NameWrapper? = null,
    @Json(name = "studyMajor") val studyMajor: NameWrapper? = null
)

@JsonClass(generateAdapter = true)
data class NameWrapper(
    @Json(name = "name") val name: String? = null
)
