package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_ID = "samad_meal_reminder"
    const val CHANNEL_NAME = "یادآور غذای سماد"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "اعلان ۳۰ دقیقه قبل از شروع زمان سرو غذا"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showMealReminderNotification(
        context: Context,
        notificationId: Int,
        mealType: String,
        servingTime: String,
        foodName: String,
        selfName: String,
        code: String?
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🍽️ یادآور وعده $mealType ($servingTime)"
        val content = if (!code.isNullOrBlank()) {
            "غذا: $foodName در $selfName\nکد فراموشی: $code (برای اسکن سریع کلیک کنید)"
        } else {
            "غذا: $foodName در $selfName\nبرای مشاهده بارکد کلیک کنید."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("وعده $mealType تا ۳۰ دقیقه دیگر سرو می‌شود.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.notify(notificationId, notification)
    }

    fun showAppUpdateNotification(
        context: Context,
        version: String,
        changelog: String?,
        downloadUrl: String?
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_UPDATE_DIALOG", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🚀 بروزرسانی جدید موجود است: نسخه $version"
        val bodyText = changelog?.take(150)?.ifBlank { "نسخه جدید سامانه سماد آماده دریافت است." }
            ?: "نسخه جدید سامانه سماد آماده دریافت است."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("برای دانلود و نصب نسخه جدید کلیک کنید.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("تغییرات:\n$bodyText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.notify(9999, notification)
    }
}
