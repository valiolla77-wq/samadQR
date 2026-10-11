package com.example.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PersianDateUtil {

    private val PERSIAN_MONTH_NAMES = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    private val PERSIAN_WEEKDAY_NAMES = mapOf(
        "MONDAY" to "دوشنبه",
        "TUESDAY" to "سه‌شنبه",
        "WEDNESDAY" to "چهارشنبه",
        "THURSDAY" to "پنج‌شنبه",
        "FRIDAY" to "جمعه",
        "SATURDAY" to "شنبه",
        "SUNDAY" to "یکشنبه"
    )

    data class PersianDate(
        val year: Int,
        val month: Int,
        val day: Int
    ) {
        val monthName: String get() = PERSIAN_MONTH_NAMES.getOrElse(month - 1) { "" }

        fun toFormattedString(): String = String.format("%04d/%02d/%02d", year, month, day)

        fun toLongFormat(dayName: String = ""): String {
            val prefix = if (dayName.isNotBlank()) "$dayName " else ""
            return "$prefix$day $monthName $year"
        }
    }

    /**
     * Converts a LocalDate (Gregorian) to Persian Solar Hijri Date
     */
    fun gregorianToPersian(date: LocalDate): PersianDate {
        var gYear = date.year
        var gMonth = date.monthValue
        var gDay = date.dayOfMonth

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        val isLeap = (gYear % 4 == 0 && gYear % 100 != 0) || (gYear % 400 == 0)
        if (isLeap) gDaysInMonth[1] = 29

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365L * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0..10) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i
                break
            }
            jDayNo -= jDaysInMonth[i]
            jm = i + 1
        }
        val jd = jDayNo.toInt() + 1

        return PersianDate(jy.toInt(), jm + 1, jd)
    }

    /**
     * Calculates the weekStartDate for Samad reserves API:
     * Saturday of the week with "00:00:00" separated by a SPACE.
     */
    fun getWeekStart(targetDate: LocalDate = LocalDate.now()): String {
        // شنبه = 5 در java.time (Mon=1..Sun=7) → شنبه در ISO = 6
        val daysSinceSaturday = (targetDate.dayOfWeek.value - 6 + 7) % 7
        val saturday = targetDate.minusDays(daysSinceSaturday.toLong())
        return "$saturday 00:00:00"  // با فاصله!
    }

    /**
     * Computes the Saturday of the week for a given LocalDate.
     * Saturday is day 0 of the Persian week.
     */
    fun getWeekStartSaturday(date: LocalDate = LocalDate.now()): LocalDate {
        // DayOfWeek: Monday=1, Tuesday=2, Wednesday=3, Thursday=4, Friday=5, Saturday=6, Sunday=7
        val daysSinceSaturday = (date.dayOfWeek.value - 6 + 7) % 7
        return date.minusDays(daysSinceSaturday.toLong())
    }

    /**
     * Returns weekStartDate formatted for Samad API: "2026-09-26 00:00:00"
     * Space between date and time is mandatory!
     */
    fun formatWeekStartDate(saturday: LocalDate): String {
        return "$saturday 00:00:00"
    }

    fun getDayOfWeekPersian(date: LocalDate): String {
        return PERSIAN_WEEKDAY_NAMES[date.dayOfWeek.name] ?: ""
    }

    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
