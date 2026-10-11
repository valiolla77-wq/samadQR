package com.example.fcm

import android.util.Log
import com.example.data.security.SecurePrefs
import com.example.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SamadFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Token received: $token")
        val prefs = SecurePrefs(applicationContext)
        prefs.fcmToken = token
        FcmHelper.syncTopicSubscriptions(applicationContext, prefs)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val prefs = SecurePrefs(applicationContext)
        if (!prefs.mealReminderNotificationEnabled || !prefs.fcmEnabled) {
            Log.d(TAG, "Meal reminders or FCM disabled by user, ignoring message")
            return
        }

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val mealType = data["meal_type"] ?: data["meal"] ?: "غذا"
        val foodName = data["food_name"] ?: data["food"] ?: notification?.body ?: "غذای رزرو شده شما"
        val servingTime = data["serving_time"] ?: data["time"] ?: "ساعت سرو"
        val selfName = data["self_name"] ?: data["self"] ?: "سلف دانشگاه"
        val forgotCode = data["forgot_code"] ?: data["code"]
        val notificationId = (data["reserve_id"]?.toIntOrNull()) ?: System.currentTimeMillis().toInt()

        val displayTitle = notification?.title ?: "🍽️ یادآور وعده $mealType ($servingTime)"
        val displayBody = notification?.body ?: "زمان سرو $foodName در $selfName آغاز می‌شود."

        prefs.lastFcmNotificationBody = "$displayTitle: $displayBody"

        NotificationHelper.showMealReminderNotification(
            context = applicationContext,
            notificationId = notificationId,
            mealType = mealType,
            servingTime = servingTime,
            foodName = foodName,
            selfName = selfName,
            code = forgotCode
        )
    }

    companion object {
        private const val TAG = "SamadFcmService"
    }
}
