package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Dining Hall / Cafeteria item returned from /rest/reservations/selfs
 */
@JsonClass(generateAdapter = true)
data class SelfItem(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "selfType") val selfType: String? = null
)

@JsonClass(generateAdapter = true)
data class SelfsResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "payload") val payload: List<SelfItem>? = null
)

/**
 * User Nurture Profile returned from /rest/users/nurture-profiles
 * containing live account wallet credit
 */
@JsonClass(generateAdapter = true)
data class NurtureProfileUser(
    @Json(name = "firstName") val firstName: String? = null,
    @Json(name = "lastName") val lastName: String? = null,
    @Json(name = "username") val username: String? = null
)

@JsonClass(generateAdapter = true)
data class NurtureProfilePayload(
    @Json(name = "credit") val credit: Long = 0L,
    @Json(name = "user") val user: NurtureProfileUser? = null
)

@JsonClass(generateAdapter = true)
data class NurtureProfileResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "payload") val payload: NurtureProfilePayload? = null
)

/**
 * Reservable weekly food program returned from /rest/programs/v2
 */
@JsonClass(generateAdapter = true)
data class ProgramFoodItem(
    @Json(name = "programId") val programId: Long,
    @Json(name = "date") val date: String,
    @Json(name = "dayTranslated") val dayTranslated: String? = null,
    @Json(name = "foodId") val foodId: Long = 0L,
    @Json(name = "foodName") val foodName: String,
    @Json(name = "foodTypeId") val foodTypeId: Int = 1,
    @Json(name = "mealTypeId") val mealTypeId: Int = 2,
    @Json(name = "price") val price: Long = 0L,
    @Json(name = "selfId") val selfId: Int = 1,
    @Json(name = "selfName") val selfName: String? = null,
    @Json(name = "reserveRuleViolated") val reserveRuleViolated: Boolean = false,
    @Json(name = "cancelRuleViolated") val cancelRuleViolated: Boolean = false,
    @Json(name = "daysDifferenceWithToday") val daysDifferenceWithToday: Int = 0
)

@JsonClass(generateAdapter = true)
data class UserWeekReserveItem(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "foodId") val foodId: Long? = null,
    @Json(name = "foodNames") val foodNames: String? = null,
    @Json(name = "programDate") val programDate: String? = null,
    @Json(name = "programId") val programId: Long? = null,
    @Json(name = "mealTypeId") val mealTypeId: Int? = null,
    @Json(name = "price") val price: Long? = null,
    @Json(name = "selfId") val selfId: Int? = null,
    @Json(name = "selfName") val selfName: String? = null
)

@JsonClass(generateAdapter = true)
data class ReserveProgramPayload(
    @Json(name = "selfWeekPrograms") val selfWeekPrograms: List<List<ProgramFoodItem>>? = null,
    @Json(name = "userWeekReserves") val userWeekReserves: List<UserWeekReserveItem>? = null
)

@JsonClass(generateAdapter = true)
data class ReserveProgramResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "payload") val payload: ReserveProgramPayload? = null
)

/**
 * Body for submitting a reservation to PUT /rest/reserves/{programId}/reserve
 */
@JsonClass(generateAdapter = true)
data class ReserveRequestBody(
    @Json(name = "foodTypeId") val foodTypeId: Int = 1,
    @Json(name = "mealTypeId") val mealTypeId: Int = 2,
    @Json(name = "selectedCount") val selectedCount: Int = 1,
    @Json(name = "freeFoodSelected") val freeFoodSelected: Boolean = false,
    @Json(name = "selected") val selected: Boolean = true
)

/**
 * Body for submitting a direct reservation to POST /rest/reserves
 */
@JsonClass(generateAdapter = true)
data class ReserveDirectBody(
    @Json(name = "programId") val programId: Long,
    @Json(name = "foodTypeId") val foodTypeId: Int = 1,
    @Json(name = "mealTypeId") val mealTypeId: Int = 2,
    @Json(name = "selectedCount") val selectedCount: Int = 1,
    @Json(name = "freeFoodSelected") val freeFoodSelected: Boolean = false,
    @Json(name = "selected") val selected: Boolean = true
)

