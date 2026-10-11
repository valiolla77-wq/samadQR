package com.example.data.model

import com.example.data.local.entity.CachedReserveEntity
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SharedTokenItem(
    val reserveId: Long = 0L,
    val date: String = "",
    val dateJStr: String = "",
    val dayName: String = "",
    val mealTypeId: Int = 1,
    val mealName: String = "",
    val foodName: String = "",
    val besideFoodNames: String = "",
    val selfName: String = "",
    val forgotCardCode: String = "",
    val price: Long = 0L
)

@JsonClass(generateAdapter = true)
data class TokenPacket(
    val packetId: String = "",
    val type: String = "SINGLE_MEAL", // "SINGLE_MEAL", "DAY", "WEEK", "ALL", "TEXT_ONLY"
    val title: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val items: List<SharedTokenItem> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromReserve(
            reserve: CachedReserveEntity,
            senderId: String,
            senderName: String,
            receiverId: String,
            messageText: String = ""
        ): TokenPacket {
            val item = SharedTokenItem(
                reserveId = reserve.reserveId,
                date = reserve.date,
                dateJStr = reserve.dateJStr,
                dayName = reserve.dayName,
                mealTypeId = reserve.mealTypeId,
                mealName = reserve.mealName,
                foodName = reserve.foodName,
                besideFoodNames = reserve.besideFoodNames,
                selfName = reserve.selfName,
                forgotCardCode = reserve.forgotCardCode.orEmpty(),
                price = reserve.price
            )
            return TokenPacket(
                packetId = "pkt_${System.currentTimeMillis()}_${(100..999).random()}",
                type = "SINGLE_MEAL",
                title = "ژتون ${reserve.mealName} - ${reserve.foodName}",
                senderId = senderId,
                senderName = senderName,
                receiverId = receiverId,
                messageText = messageText,
                items = listOf(item),
                timestamp = System.currentTimeMillis()
            )
        }

        fun fromGroup(
            reserves: List<CachedReserveEntity>,
            type: String, // "DAY", "WEEK", "ALL"
            title: String,
            senderId: String,
            senderName: String,
            receiverId: String,
            messageText: String = ""
        ): TokenPacket {
            val items = reserves.map { reserve ->
                SharedTokenItem(
                    reserveId = reserve.reserveId,
                    date = reserve.date,
                    dateJStr = reserve.dateJStr,
                    dayName = reserve.dayName,
                    mealTypeId = reserve.mealTypeId,
                    mealName = reserve.mealName,
                    foodName = reserve.foodName,
                    besideFoodNames = reserve.besideFoodNames,
                    selfName = reserve.selfName,
                    forgotCardCode = reserve.forgotCardCode.orEmpty(),
                    price = reserve.price
                )
            }
            return TokenPacket(
                packetId = "pkt_${System.currentTimeMillis()}_${(100..999).random()}",
                type = type,
                title = title,
                senderId = senderId,
                senderName = senderName,
                receiverId = receiverId,
                messageText = messageText,
                items = items,
                timestamp = System.currentTimeMillis()
            )
        }
    }
}
