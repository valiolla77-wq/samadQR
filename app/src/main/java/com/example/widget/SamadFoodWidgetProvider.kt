package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CachedReserveEntity
import com.example.data.security.SecurePrefs
import com.example.util.MealTimeHelper
import com.example.util.PersianDateUtil
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class SamadFoodWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_CYCLE_WIDGET_MEAL -> {
                val widgetPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val current = widgetPrefs.getInt(KEY_CYCLE_INDEX, 0)
                widgetPrefs.edit()
                    .putInt(KEY_CYCLE_INDEX, current + 1)
                    .putLong(KEY_CYCLE_TIMESTAMP, System.currentTimeMillis())
                    .apply()
                updateAllWidgets(context)
            }
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED -> {
                updateAllWidgets(context)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val ACTION_CYCLE_WIDGET_MEAL = "com.example.samad.ACTION_CYCLE_WIDGET_MEAL"
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_CYCLE_INDEX = "cycle_index"
        private const val KEY_CYCLE_TIMESTAMP = "cycle_timestamp"
        private const val TAG = "SamadWidgetProvider"

        fun requestPinWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, SamadFoodWidgetProvider::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val pinnedWidgetCallbackIntent = Intent(context, SamadFoodWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        0,
                        pinnedWidgetCallbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                    android.widget.Toast.makeText(context, "درخواست افزودن ویجت به صفحه اصلی ارسال شد", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "لانچر دستگاه شما از افزودن خودکار ویجت پشتیبانی نمی‌کند. لطفاً از لیست ویجت‌ها اضافه کنید.", android.widget.Toast.LENGTH_LONG).show()
                }
            } else {
                android.widget.Toast.makeText(context, "لطفاً از طریق صفحه اصلی، ویجت سماد را اضافه کنید.", android.widget.Toast.LENGTH_LONG).show()
            }
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, SamadFoodWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            for (id in appWidgetIds) {
                updateWidget(context, appWidgetManager, id)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_samad_food)

            // Pending intent to open MainActivity on widget body or QR tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_qr_image, pendingIntent)

            // Tapping meal title cycles through today's meals if multiple exist
            val cycleIntent = Intent(context, SamadFoodWidgetProvider::class.java).apply {
                action = ACTION_CYCLE_WIDGET_MEAL
            }
            val cyclePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                cycleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_meal_title, cyclePendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getInstance(context)
                val prefs = SecurePrefs(context)
                val now = LocalDateTime.now()
                val todayStr = now.toLocalDate().toString()

                val todayReserves = db.reserveDao().getReservesForDateDirect(todayStr)
                val futureReserves = db.reserveDao().getUpcomingUnconsumedReservesDirect(todayStr)

                // Check manual cycle: if user clicked cycle within last 10 minutes and today has multiple meals
                val widgetPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val cycleTimestamp = widgetPrefs.getLong(KEY_CYCLE_TIMESTAMP, 0L)
                val isRecentCycle = (System.currentTimeMillis() - cycleTimestamp) < (10 * 60 * 1000L)

                val selectedReserve: CachedReserveEntity? = if (isRecentCycle && todayReserves.size > 1) {
                    val cycleIndex = widgetPrefs.getInt(KEY_CYCLE_INDEX, 0)
                    todayReserves[cycleIndex % todayReserves.size]
                } else {
                    MealTimeHelper.findBestReserveForTime(
                        todayReserves = todayReserves,
                        allFutureReserves = futureReserves,
                        prefs = prefs,
                        now = now
                    )
                }

                if (selectedReserve != null) {
                    val code = selectedReserve.forgotCardCode ?: ""
                    val formattedCode = QrCodeGenerator.formatNineDigitCode(code, toPersian = true)
                    val status = MealTimeHelper.calculateMealStatus(
                        dateStr = selectedReserve.date,
                        mealName = selectedReserve.mealName,
                        prefs = prefs,
                        nowDateTime = now
                    )

                    val isToday = selectedReserve.date == todayStr
                    val dayHeader = if (isToday) {
                        if (todayReserves.size > 1) "${selectedReserve.mealName} ⇄" else selectedReserve.mealName
                    } else {
                        try {
                            val ld = LocalDate.parse(selectedReserve.date)
                            val dayFa = PersianDateUtil.getDayOfWeekPersian(ld)
                            "$dayFa ${selectedReserve.mealName}"
                        } catch (e: Exception) {
                            selectedReserve.mealName
                        }
                    }

                    val statusLabel = if (selectedReserve.consumed) "مصرف‌شده" else status.statusText
                    views.setTextViewText(R.id.widget_meal_title, "$dayHeader • $statusLabel")
                    views.setTextViewText(R.id.widget_food_name, selectedReserve.foodName.ifBlank { selectedReserve.selfName })
                    views.setTextViewText(R.id.widget_code_text, formattedCode.ifBlank { "کد در دسترس نیست" })
                    views.setTextViewText(R.id.widget_status_text, selectedReserve.selfName)

                    if (code.isNotBlank()) {
                        val qrBitmap = QrCodeGenerator.generateQrBitmap(code, size = 600)
                        if (qrBitmap != null) {
                            views.setImageViewBitmap(R.id.widget_qr_image, qrBitmap)
                        }
                    }
                } else {
                    views.setTextViewText(R.id.widget_meal_title, "سماد فود")
                    views.setTextViewText(R.id.widget_food_name, context.getString(R.string.widget_no_food))
                    views.setTextViewText(R.id.widget_code_text, "--- --- ---")
                    views.setTextViewText(R.id.widget_status_text, "برای دریافت رزروها وارد شوید")
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)

                // Schedule alarm for next meal transition
                scheduleNextTransitionAlarm(context, todayReserves + futureReserves, prefs, now)
            }
        }

        private fun scheduleNextTransitionAlarm(
            context: Context,
            reserves: List<CachedReserveEntity>,
            prefs: SecurePrefs,
            now: LocalDateTime
        ) {
            val nextMillis = MealTimeHelper.getNextMealTransitionMillis(reserves, prefs, now) ?: return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, SamadFoodWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                8888,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, nextMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC, nextMillis, pendingIntent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to schedule widget transition alarm: ${e.message}")
            }
        }
    }
}

