package com.example

import android.app.Application
import com.example.notification.NotificationHelper
import com.example.work.WorkScheduler

class SamadApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        WorkScheduler.schedulePeriodicSync(this)
    }
}
