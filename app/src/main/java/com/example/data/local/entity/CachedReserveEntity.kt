package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_reserves")
data class CachedReserveEntity(
    @PrimaryKey val reserveId: Long,
    val programId: Long = 0L,
    val date: String,
    val dateJStr: String,
    val dayName: String,
    val mealTypeId: Int,
    val mealName: String,
    val foodName: String,
    val besideFoodNames: String = "",
    val selfName: String = "",
    val selfCode: String = "",
    val price: Long = 0L,
    val consumed: Boolean = false,
    val forgotCardCode: String? = null,
    val codeValid: Boolean = true,
    val isGuest: Boolean = false,
    val guestOwnerName: String? = null,
    val syncTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_meal_types")
data class CachedMealTypeEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val disPriority: Int = 0
)
