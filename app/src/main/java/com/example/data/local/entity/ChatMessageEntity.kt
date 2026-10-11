package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageText: String = "",
    val packetType: String = "TEXT_ONLY", // "TEXT_ONLY", "SINGLE_MEAL", "DAY", "WEEK", "ALL"
    val packetTitle: String = "",
    val tokenPacketJson: String? = null,
    val isOutgoing: Boolean = false,
    val isRead: Boolean = false,
    val isClaimed: Boolean = false, // If user added the token to their reserves
    val status: String = "delivered" // "pending", "sent", "delivered", "failed"
)
