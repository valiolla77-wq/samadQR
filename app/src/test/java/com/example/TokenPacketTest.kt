package com.example

import com.example.data.model.SharedTokenItem
import com.example.data.model.TokenPacket
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenPacketTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(TokenPacket::class.java)

    @Test
    fun testTokenPacketSerializationAndDeserialization() {
        val item = SharedTokenItem(
            reserveId = 12345L,
            date = "2026-10-12",
            dateJStr = "1405/07/21",
            dayName = "دوشنبه",
            mealTypeId = 7,
            mealName = "ناهار",
            foodName = "چلو کباب کوبیده",
            besideFoodNames = "ماست و دوغ",
            selfName = "سلف مرکزی",
            forgotCardCode = "987654321",
            price = 25000L
        )

        val packet = TokenPacket(
            packetId = "pkt_test_1001",
            type = "SINGLE_MEAL",
            title = "ژتون ناهار - چلو کباب کوبیده",
            senderId = "student_user_1",
            senderName = "علی رضایی",
            receiverId = "student_user_2",
            messageText = "بفرمایید ناهار امروز",
            items = listOf(item),
            timestamp = 1791800000000L
        )

        val json = adapter.toJson(packet)
        assertNotNull(json)
        assertTrue(json.contains("pkt_test_1001"))
        assertTrue(json.contains("چلو کباب کوبیده"))

        val deserialized = adapter.fromJson(json)
        assertNotNull(deserialized)
        assertEquals(packet.packetId, deserialized?.packetId)
        assertEquals(packet.type, deserialized?.type)
        assertEquals(packet.title, deserialized?.title)
        assertEquals(packet.senderId, deserialized?.senderId)
        assertEquals(packet.senderName, deserialized?.senderName)
        assertEquals(packet.receiverId, deserialized?.receiverId)
        assertEquals(1, deserialized?.items?.size)

        val firstItem = deserialized?.items?.first()
        assertEquals(12345L, firstItem?.reserveId)
        assertEquals("ناهار", firstItem?.mealName)
        assertEquals("987654321", firstItem?.forgotCardCode)
    }

    @Test
    fun testEmptyItemsPacket() {
        val packet = TokenPacket(
            packetId = "pkt_text_only",
            type = "TEXT_ONLY",
            title = "پیام متنی",
            senderId = "user1",
            senderName = "سارا",
            receiverId = "user2",
            messageText = "سلام",
            items = emptyList()
        )

        val json = adapter.toJson(packet)
        val deserialized = adapter.fromJson(json)

        assertNotNull(deserialized)
        assertEquals("pkt_text_only", deserialized?.packetId)
        assertTrue(deserialized?.items?.isEmpty() == true)
    }
}
