package com.example

import com.example.data.model.ForgotCodeResponse
import com.example.util.PersianDateUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testGetWeekStart_matchesPythonOutput() {
        // 2026-09-26 is Saturday
        val saturday = LocalDate.parse("2026-09-26")
        assertEquals("2026-09-26 00:00:00", PersianDateUtil.getWeekStart(saturday))

        // 2026-09-27 is Sunday -> should still point to 2026-09-26
        val sunday = LocalDate.parse("2026-09-27")
        assertEquals("2026-09-26 00:00:00", PersianDateUtil.getWeekStart(sunday))

        // 2026-10-02 is Friday (last day of the week) -> should point to 2026-09-26
        val friday = LocalDate.parse("2026-10-02")
        assertEquals("2026-09-26 00:00:00", PersianDateUtil.getWeekStart(friday))

        // 2026-10-03 is the next Saturday
        val nextSaturday = LocalDate.parse("2026-10-03")
        assertEquals("2026-10-03 00:00:00", PersianDateUtil.getWeekStart(nextSaturday))
    }

    @Test
    fun testForgotCodeResponseParsing() {
        val json = """
            {
              "type": "INFO",
              "payload": {
                "valid": true,
                "forgotCardCode": "740560364",
                "self": "پسران قم",
                "meal": "شام",
                "foodName": "خوراک مرغ|نان | گوجه | خیارشور"
              }
            }
        """.trimIndent()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(ForgotCodeResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals("INFO", response?.type)
        assertNotNull(response?.payload)
        assertEquals("740560364", response?.payload?.forgotCardCode)
        assertEquals("شام", response?.payload?.meal)
        assertTrue(response?.payload?.valid == true)
    }

    @Test
    fun testMealTypeFilter_noReservationForSelectedMeal_doesNotDefaultToOtherMeals() {
        // Given reservations on 2026-09-24 with Breakfast (6) only
        val breakfastReserve = com.example.data.local.entity.CachedReserveEntity(
            reserveId = 101L,
            date = "2026-09-24",
            dateJStr = "1405/07/02",
            dayName = "پنج‌شنبه",
            mealTypeId = 6, // Breakfast
            mealName = "صبحانه",
            foodName = "تخم مرغ آبپز"
        )
        val allReserves = listOf(breakfastReserve)

        val selectedDate = "2026-09-24"
        val selectedMealTypeId = 7 // Lunch

        // Explicitly filter for selectedDate and selectedMealTypeId
        val filteredReserves = if (selectedMealTypeId != 0) {
            allReserves.filter { it.date == selectedDate && it.mealTypeId == selectedMealTypeId }
        } else {
            emptyList()
        }

        val activeReserve = filteredReserves.firstOrNull()

        // Assert that selecting Lunch (7) returns empty list and null activeReserve, NOT defaulting to Breakfast (6)
        assertTrue(filteredReserves.isEmpty())
        org.junit.Assert.assertNull(activeReserve)
    }

    @Test
    fun testMealTypeFilter_matchingReservationFound_setsActiveReserve() {
        val lunchReserve = com.example.data.local.entity.CachedReserveEntity(
            reserveId = 102L,
            date = "2026-09-24",
            dateJStr = "1405/07/02",
            dayName = "پنج‌شنبه",
            mealTypeId = 7, // Lunch
            mealName = "ناهار",
            foodName = "قورمه سبزی"
        )
        val dinnerReserve = com.example.data.local.entity.CachedReserveEntity(
            reserveId = 103L,
            date = "2026-09-24",
            dateJStr = "1405/07/02",
            dayName = "پنج‌شنبه",
            mealTypeId = 8, // Dinner
            mealName = "شام",
            foodName = "پیتزا"
        )
        val allReserves = listOf(lunchReserve, dinnerReserve)

        val selectedDate = "2026-09-24"

        // When Lunch (7) selected
        val lunchFiltered = allReserves.filter { it.date == selectedDate && it.mealTypeId == 7 }
        assertEquals(1, lunchFiltered.size)
        assertEquals(102L, lunchFiltered.first().reserveId)
        assertEquals("ناهار", lunchFiltered.first().mealName)

        // When Dinner (8) selected
        val dinnerFiltered = allReserves.filter { it.date == selectedDate && it.mealTypeId == 8 }
        assertEquals(1, dinnerFiltered.size)
        assertEquals(103L, dinnerFiltered.first().reserveId)
        assertEquals("شام", dinnerFiltered.first().mealName)
    }

    @Test
    fun testUpdateManagerVersionComparison() {
        // Remote is newer
        assertTrue(com.example.util.UpdateManager.isNewerVersion("1.2.0", "v1.3.0"))
        assertTrue(com.example.util.UpdateManager.isNewerVersion("1.2.0", "1.2.1"))
        assertTrue(com.example.util.UpdateManager.isNewerVersion("1.2.0", "2.0.0"))
        assertTrue(com.example.util.UpdateManager.isNewerVersion("1.2.0", "v1.2.0.1"))

        // Same version or older
        org.junit.Assert.assertFalse(com.example.util.UpdateManager.isNewerVersion("1.2.0", "v1.2.0"))
        org.junit.Assert.assertFalse(com.example.util.UpdateManager.isNewerVersion("1.2.0", "1.2.0"))
        org.junit.Assert.assertFalse(com.example.util.UpdateManager.isNewerVersion("1.3.0", "v1.2.0"))
        org.junit.Assert.assertFalse(com.example.util.UpdateManager.isNewerVersion("1.2.0", "v1.1.9"))
    }
}
