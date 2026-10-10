package com.example.fcm

import android.content.Context
import android.util.Log
import com.example.data.security.SecurePrefs
import com.example.notification.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

object FcmHelper {

    private const val TAG = "FcmHelper"

    const val TOPIC_ALL_MEALS = "meal_reminders_all"
    const val TOPIC_BREAKFAST = "meal_breakfast"
    const val TOPIC_LUNCH = "meal_lunch"
    const val TOPIC_DINNER = "meal_dinner"

    fun isFirebaseInitialized(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    fun initAndFetchToken(context: Context, onTokenRetrieved: (String?) -> Unit) {
        val prefs = SecurePrefs(context)
        // If we already have a cached token, return it immediately
        val cached = prefs.fcmToken
        if (!cached.isNullOrBlank()) {
            onTokenRetrieved(cached)
        }

        try {
            if (!isFirebaseInitialized(context)) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseApp.initializeApp failed: ${e.message}")
                }
            }

            if (isFirebaseInitialized(context)) {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            Log.i(TAG, "FCM Registration Token: $token")
                            prefs.fcmToken = token
                            syncTopicSubscriptions(context, prefs)
                            onTokenRetrieved(token)
                        } else {
                            Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                            onTokenRetrieved(prefs.fcmToken)
                        }
                    }
            } else {
                Log.w(TAG, "Firebase is not initialized (google-services.json not configured yet)")
                onTokenRetrieved(prefs.fcmToken)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during FCM token retrieval: ${e.message}")
            onTokenRetrieved(prefs.fcmToken)
        }
    }

    fun syncTopicSubscriptions(context: Context, prefs: SecurePrefs) {
        if (!isFirebaseInitialized(context)) return

        try {
            val fm = FirebaseMessaging.getInstance()
            if (prefs.fcmEnabled && prefs.mealReminderNotificationEnabled) {
                // Subscribe to general meal reminder broadcast
                fm.subscribeToTopic(TOPIC_ALL_MEALS)
                    .addOnSuccessListener { Log.d(TAG, "Subscribed to $TOPIC_ALL_MEALS") }

                if (prefs.cafeteriaBreakfastEnabled) {
                    fm.subscribeToTopic(TOPIC_BREAKFAST)
                } else {
                    fm.unsubscribeFromTopic(TOPIC_BREAKFAST)
                }

                if (prefs.cafeteriaLunchEnabled) {
                    fm.subscribeToTopic(TOPIC_LUNCH)
                } else {
                    fm.unsubscribeFromTopic(TOPIC_LUNCH)
                }

                if (prefs.cafeteriaDinnerEnabled) {
                    fm.subscribeToTopic(TOPIC_DINNER)
                } else {
                    fm.unsubscribeFromTopic(TOPIC_DINNER)
                }
            } else {
                // Unsubscribe if user disabled reminders
                fm.unsubscribeFromTopic(TOPIC_ALL_MEALS)
                fm.unsubscribeFromTopic(TOPIC_BREAKFAST)
                fm.unsubscribeFromTopic(TOPIC_LUNCH)
                fm.unsubscribeFromTopic(TOPIC_DINNER)
                Log.d(TAG, "Unsubscribed from all meal topics")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing topic subscriptions: ${e.message}")
        }
    }

    fun sendTestMealNotification(context: Context) {
        NotificationHelper.showMealReminderNotification(
            context = context,
            notificationId = 99999,
            mealType = "ناهار (تست فایربیس)",
            servingTime = "۱۱:۳۰ تا ۱۳:۴۵",
            foodName = "چلو کباب کوبیده با ماست و سالاد",
            selfName = "سلف مرکزی دانشگاه",
            code = "987654321"
        )
    }
}
