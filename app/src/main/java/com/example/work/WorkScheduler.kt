package com.example.work

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val TAG = "WorkScheduler"
    private const val PERIODIC_SYNC_WORK_NAME = "samad_periodic_sync_work"

    fun schedulePeriodicSync(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // Periodic sync every 6 hours
            val periodicRequest = PeriodicWorkRequestBuilder<SyncReservesWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not initialize or schedule periodic sync: ${e.message}")
        }
    }

    fun scheduleMealReminder(
        context: Context,
        reserveId: Long,
        mealType: String,
        servingTime: String,
        foodName: String,
        selfName: String,
        code: String?,
        triggerAt: LocalDateTime
    ) {
        try {
            val now = LocalDateTime.now()
            val delayMillis = Duration.between(now, triggerAt).toMillis()
            if (delayMillis <= 0) return

            val data = Data.Builder()
                .putString(MealReminderWorker.KEY_MEAL_TYPE, mealType)
                .putString(MealReminderWorker.KEY_SERVING_TIME, servingTime)
                .putString(MealReminderWorker.KEY_FOOD_NAME, foodName)
                .putString(MealReminderWorker.KEY_SELF_NAME, selfName)
                .putString(MealReminderWorker.KEY_CODE, code)
                .putLong(MealReminderWorker.KEY_RESERVE_ID, reserveId)
                .build()

            val request = OneTimeWorkRequestBuilder<MealReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "meal_reminder_$reserveId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not schedule meal reminder for $reserveId: ${e.message}")
        }
    }

    fun cancelAllReminders(context: Context) {
        try {
            WorkManager.getInstance(context).cancelAllWorkByTag("meal_reminder")
        } catch (e: Exception) {
            Log.w(TAG, "Could not cancel meal reminders: ${e.message}")
        }
    }
}
