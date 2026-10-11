package com.example.util

import com.example.data.local.entity.CachedReserveEntity
import com.example.data.security.SecurePrefs
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object MealTimeHelper {

    enum class MealCategory(val faName: String, val defaultMealTypeId: Int) {
        BREAKFAST("صبحانه", 6),
        LUNCH("ناهار", 7),
        DINNER("شام", 8),
        IFTAR("افطاری", 9),
        SUHUR("سحری", 10),
        UNKNOWN("سایر", 0);

        companion object {
            fun fromName(name: String): MealCategory {
                return when {
                    name.contains("صبحانه") -> BREAKFAST
                    name.contains("ناهار") -> LUNCH
                    name.contains("شام") -> DINNER
                    name.contains("افطار") -> IFTAR
                    name.contains("سحر") -> SUHUR
                    else -> UNKNOWN
                }
            }
        }
    }

    data class ServingWindow(
        val category: MealCategory,
        val startTime: LocalTime,
        val endTime: LocalTime
    ) {
        fun formatRange(): String {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return "${startTime.format(formatter)} تا ${endTime.format(formatter)}"
        }
    }

    data class MealStatusInfo(
        val statusText: String,
        val isActive: Boolean,
        val isExpired: Boolean,
        val isUpcoming: Boolean,
        val minutesUntilStart: Long = 0L
    )

    fun getServingWindow(category: MealCategory, prefs: SecurePrefs): ServingWindow {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val (startStr, endStr) = when (category) {
            MealCategory.BREAKFAST -> prefs.breakfastStart to prefs.breakfastEnd
            MealCategory.LUNCH -> prefs.lunchStart to prefs.lunchEnd
            MealCategory.DINNER -> prefs.dinnerStart to prefs.dinnerEnd
            MealCategory.IFTAR -> prefs.iftarStart to prefs.iftarEnd
            MealCategory.SUHUR -> prefs.suhurStart to prefs.suhurEnd
            MealCategory.UNKNOWN -> "11:30" to "14:00"
        }
        val start = try { LocalTime.parse(startStr, formatter) } catch (e: Exception) { LocalTime.of(11, 30) }
        val end = try { LocalTime.parse(endStr, formatter) } catch (e: Exception) { LocalTime.of(14, 0) }
        return ServingWindow(category, start, end)
    }

    /**
     * Determines the status of a meal given its date ("yyyy-MM-dd") and meal name/category.
     */
    fun calculateMealStatus(
        dateStr: String,
        mealName: String,
        prefs: SecurePrefs,
        nowDateTime: LocalDateTime = LocalDateTime.now()
    ): MealStatusInfo {
        val category = MealCategory.fromName(mealName)
        val window = getServingWindow(category, prefs)

        val mealDate = try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val mealStartDateTime = LocalDateTime.of(mealDate, window.startTime)
        val mealEndDateTime = LocalDateTime.of(mealDate, window.endTime)

        return when {
            // Already past end time
            nowDateTime.isAfter(mealEndDateTime) -> {
                MealStatusInfo(
                    statusText = "منقضی شده",
                    isActive = false,
                    isExpired = true,
                    isUpcoming = false
                )
            }
            // Currently within serving window
            nowDateTime.isEqual(mealStartDateTime) ||
                    (nowDateTime.isAfter(mealStartDateTime) && nowDateTime.isBefore(mealEndDateTime)) -> {
                MealStatusInfo(
                    statusText = "فعال",
                    isActive = true,
                    isExpired = false,
                    isUpcoming = false
                )
            }
            // Upcoming
            else -> {
                val duration = Duration.between(nowDateTime, mealStartDateTime)
                val totalMinutes = duration.toMinutes()
                val hours = totalMinutes / 60
                val mins = totalMinutes % 60

                val statusText = when {
                    hours > 24 -> "تا ${hours / 24} روز دیگر"
                    hours > 0 -> "تا $hours ساعت دیگر"
                    totalMinutes > 0 -> "تا $mins دقیقه دیگر"
                    else -> "به زودی"
                }

                MealStatusInfo(
                    statusText = statusText,
                    isActive = false,
                    isExpired = false,
                    isUpcoming = true,
                    minutesUntilStart = totalMinutes
                )
            }
        }
    }

    /**
     * Identifies which meal type is active right now based on local time
     */
    fun getActiveMealCategory(prefs: SecurePrefs, nowTime: LocalTime = LocalTime.now()): MealCategory {
        val categories = listOf(
            MealCategory.SUHUR,
            MealCategory.BREAKFAST,
            MealCategory.LUNCH,
            MealCategory.IFTAR,
            MealCategory.DINNER
        )

        // First check if any category is strictly active
        for (cat in categories) {
            val window = getServingWindow(cat, prefs)
            if (!nowTime.isBefore(window.startTime) && nowTime.isBefore(window.endTime)) {
                return cat
            }
        }

        // Otherwise return the next upcoming category
        for (cat in categories) {
            val window = getServingWindow(cat, prefs)
            if (nowTime.isBefore(window.startTime)) {
                return cat
            }
        }

        return MealCategory.LUNCH
    }

    /**
     * Finds the most relevant reserve for the widget and active card:
     * 1. If currently within a meal serving window, pick today's active unconsumed reserve for this meal (Breakfast/Lunch/Dinner).
     * 2. If no meal is currently active, pick the next upcoming unconsumed reserve today (earliest start time).
     * 3. If all today's meals have passed or are consumed, pick the earliest upcoming unconsumed reserve in future days.
     * 4. Fallback: return today's first reserve or the earliest available reserve.
     */
    fun findBestReserveForTime(
        todayReserves: List<CachedReserveEntity>,
        allFutureReserves: List<CachedReserveEntity>,
        prefs: SecurePrefs,
        now: LocalDateTime = LocalDateTime.now()
    ): CachedReserveEntity? {
        val todayStr = now.toLocalDate().toString()

        if (todayReserves.isNotEmpty()) {
            val statusList = todayReserves.map { res ->
                val category = MealCategory.fromName(res.mealName)
                val window = getServingWindow(category, prefs)
                val status = calculateMealStatus(
                    dateStr = res.date,
                    mealName = res.mealName,
                    prefs = prefs,
                    nowDateTime = now
                )
                ReserveWithTiming(res, category, window, status)
            }

            // 1. Check if any meal is active right now and unconsumed
            val activeUnconsumed = statusList.filter { it.status.isActive && !it.reserve.consumed }
            if (activeUnconsumed.isNotEmpty()) {
                // Prefer user's own reserve over guest if multiple, or first unconsumed
                return activeUnconsumed.find { !it.reserve.isGuest }?.reserve ?: activeUnconsumed.first().reserve
            }

            // 2. Check if any meal is upcoming today and unconsumed
            val upcomingToday = statusList.filter { it.status.isUpcoming && !it.reserve.consumed }
                .sortedBy { it.window.startTime }
            if (upcomingToday.isNotEmpty()) {
                return upcomingToday.find { !it.reserve.isGuest }?.reserve ?: upcomingToday.first().reserve
            }

            // 3. If an active meal is currently being served (even if consumed), show it so student sees consumed state
            val activeConsumed = statusList.filter { it.status.isActive }
            if (activeConsumed.isNotEmpty()) {
                return activeConsumed.first().reserve
            }
        }

        // 4. Look at upcoming unconsumed reserves starting tomorrow or in future days
        val futureUnconsumed = allFutureReserves
            .filter { it.date > todayStr && !it.consumed }
            .sortedWith(compareBy({ it.date }, { getServingWindow(MealCategory.fromName(it.mealName), prefs).startTime }))
        if (futureUnconsumed.isNotEmpty()) {
            return futureUnconsumed.first()
        }

        // 5. Fallback: if everything is consumed, return today's reserve or first available
        return todayReserves.firstOrNull() ?: allFutureReserves.firstOrNull()
    }

    data class ReserveWithTiming(
        val reserve: CachedReserveEntity,
        val category: MealCategory,
        val window: ServingWindow,
        val status: MealStatusInfo
    )

    /**
     * Calculates the timestamp in milliseconds when the next meal state transition occurs (e.g. meal start or end)
     * so that the widget can schedule an exact alarm update.
     */
    fun getNextMealTransitionMillis(
        reserves: List<CachedReserveEntity>,
        prefs: SecurePrefs,
        now: LocalDateTime = LocalDateTime.now()
    ): Long? {
        val today = now.toLocalDate()
        val transitionDateTimes = mutableListOf<LocalDateTime>()

        for (res in reserves.filter { it.date >= today.toString() }) {
            val category = MealCategory.fromName(res.mealName)
            val window = getServingWindow(category, prefs)
            val mealDate = try { LocalDate.parse(res.date) } catch (e: Exception) { today }
            val startDateTime = LocalDateTime.of(mealDate, window.startTime)
            val endDateTime = LocalDateTime.of(mealDate, window.endTime)

            if (startDateTime.isAfter(now)) {
                transitionDateTimes.add(startDateTime)
            }
            if (endDateTime.isAfter(now)) {
                transitionDateTimes.add(endDateTime)
            }
        }

        val nextTransition = transitionDateTimes.minOrNull() ?: return null
        return nextTransition.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    /**
     * Calculates the target reminder time (30 minutes before meal start).
     */
    fun getReminderTargetDateTime(
        dateStr: String,
        mealName: String,
        prefs: SecurePrefs
    ): LocalDateTime {
        val category = MealCategory.fromName(mealName)
        val window = getServingWindow(category, prefs)
        val mealDate = try { LocalDate.parse(dateStr) } catch (e: Exception) { LocalDate.now() }
        val startDateTime = LocalDateTime.of(mealDate, window.startTime)
        return startDateTime.minusMinutes(30)
    }
}
