package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_reserves")
data class PendingReserveEntity(
    @PrimaryKey
    val programId: Long,
    val foodName: String,
    val date: String,
    val mealTypeId: Int,
    val mealTypeName: String,
    val selfId: Int,
    val selfName: String,
    val foodTypeId: Int,
    val price: Long,
    val deficitAmount: Long, // کسری مبلغ شارژ به ریال
    val createdAt: Long = System.currentTimeMillis()
)
