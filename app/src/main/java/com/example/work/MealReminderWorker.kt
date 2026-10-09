package com.example.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notification.NotificationHelper

class MealReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val mealType = inputData.getString(KEY_MEAL_TYPE) ?: "غذا"
        val servingTime = inputData.getString(KEY_SERVING_TIME) ?: ""
        val foodName = inputData.getString(KEY_FOOD_NAME) ?: "غذای رزرو شده"
        val selfName = inputData.getString(KEY_SELF_NAME) ?: "سلف دانشگاه"
        val code = inputData.getString(KEY_CODE)
        val reserveId = inputData.getLong(KEY_RESERVE_ID, System.currentTimeMillis())

        NotificationHelper.showMealReminderNotification(
            context = context,
            notificationId = (reserveId % 100000).toInt(),
            mealType = mealType,
            servingTime = servingTime,
            foodName = foodName,
            selfName = selfName,
            code = code
        )

        return Result.success()
    }

    companion object {
        const val KEY_MEAL_TYPE = "meal_type"
        const val KEY_SERVING_TIME = "serving_time"
        const val KEY_FOOD_NAME = "food_name"
        const val KEY_SELF_NAME = "self_name"
        const val KEY_CODE = "code"
        const val KEY_RESERVE_ID = "reserve_id"
    }
}
