package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReservesResponse(
    @Json(name = "type") val type: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "payload") val payload: ReservesPayload? = null
)

@JsonClass(generateAdapter = true)
data class ReservesPayload(
    @Json(name = "weekDays") val weekDays: List<WeekDay> = emptyList(),
    @Json(name = "mealTypes") val mealTypes: List<MealTypeInfo> = emptyList(),
    @Json(name = "reserves") val reserves: List<Reserve>? = null,
    @Json(name = "remainCredit") val remainCredit: Long = 0L
)

@JsonClass(generateAdapter = true)
data class WeekDay(
    @Json(name = "day") val day: String = "",
    @Json(name = "dayTranslated") val dayTranslated: String = "",
    @Json(name = "date") val date: String = "",
    @Json(name = "dateJStr") val dateJStr: String = "",
    @Json(name = "mealTypes") val mealTypes: List<MealSlot> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MealSlot(
    @Json(name = "mealTypeId") val mealTypeId: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "date") val date: String = "",
    @Json(name = "reserve") val reserve: Reserve? = null,
    @Json(name = "dateTime") val dateTime: Long? = null,
    @Json(name = "hasReserve") val hasReserve: Boolean? = null,
    @Json(name = "isReserved") val isReserved: Boolean? = null,
    @Json(name = "selected") val selected: Boolean? = null,
    @Json(name = "reserves") val reserves: List<Reserve>? = null
)

@JsonClass(generateAdapter = true)
data class Reserve(
    @Json(name = "id") val id: Long? = 0L,
    @Json(name = "programId") val programId: Long? = 0L,
    @Json(name = "foodNames") val foodNames: String = "",
    @Json(name = "besideFoodNames") val besideFoodNames: String = "",
    @Json(name = "selfName") val selfName: String = "",
    @Json(name = "selfCode") val selfCode: String = "",
    @Json(name = "selfCodeName") val selfCodeName: String? = null,
    @Json(name = "price") val price: Long = 0L,
    @Json(name = "consumed") val consumed: Boolean = false,
    @Json(name = "selectedCount") val selectedCount: Int? = null,
    @Json(name = "remainedCount") val remainedCount: Int? = null,
    @Json(name = "count") val count: Int? = null,
    @Json(name = "selected") val selected: Boolean? = null,
    @Json(name = "hasReserve") val hasReserve: Boolean? = null,
    @Json(name = "isReserved") val isReserved: Boolean? = null,
    @Json(name = "reserved") val reserved: Boolean? = null,
    @Json(name = "canCancel") val canCancel: Boolean? = null,
    @Json(name = "canReserve") val canReserve: Boolean? = null,
    @Json(name = "timeDistanceUntilToday") val timeDistanceUntilToday: Int = 0,
    @Json(name = "programDateStr") val programDateStr: String = "",
    @Json(name = "programDate") val programDate: String = "",
    @Json(name = "foodTypeTitle") val foodTypeTitle: String? = null,
    @Json(name = "forgetCardCode") val forgetCardCode: String? = null,
    @Json(name = "forgotCardCode") val forgotCardCode: String? = null,
    @Json(name = "barcode") val barcode: String? = null,
    @Json(name = "printCode") val printCode: String? = null
)

@JsonClass(generateAdapter = true)
data class MealTypeInfo(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "name") val name: String = "",
    @Json(name = "canPanelDisplay") val canPanelDisplay: Boolean = true,
    @Json(name = "disPriority") val disPriority: Int = 0
)
