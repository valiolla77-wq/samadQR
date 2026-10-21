package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefs(context: Context) {

    private val prefs: SharedPreferences = createEncryptedPrefs(context.applicationContext)

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var tokenExpiryTimestamp: Long
        get() = prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
        set(value) = prefs.edit().putLong(KEY_TOKEN_EXPIRY, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var password: String?
        get() = prefs.getString(KEY_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_PASSWORD, value).apply()

    var rememberPassword: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_PW, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_PW, value).apply()

    var baseUrl: String
        get() {
            val url = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
            return if (url.endsWith("/")) url else "$url/"
        }
        set(value) {
            val normalized = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString(KEY_BASE_URL, normalized).apply()
        }

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    var studentName: String?
        get() = prefs.getString(KEY_STUDENT_NAME, null)
        set(value) = prefs.edit().putString(KEY_STUDENT_NAME, value).apply()

    var studentNumber: String?
        get() = prefs.getString(KEY_STUDENT_NUM, null)
        set(value) = prefs.edit().putString(KEY_STUDENT_NUM, value).apply()

    var studentMajor: String?
        get() = prefs.getString(KEY_STUDENT_MAJOR, null)
        set(value) = prefs.edit().putString(KEY_STUDENT_MAJOR, value).apply()

    var autoSyncDaily: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

    var autoSyncOnLaunch: Boolean
        get() = prefs.getBoolean("auto_sync_on_launch", true)
        set(value) = prefs.edit().putBoolean("auto_sync_on_launch", value).apply()

    var mealReminderNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_NOTIFICATION, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_NOTIFICATION, value).apply()

    var fcmEnabled: Boolean
        get() = prefs.getBoolean("fcm_reminders_enabled", true)
        set(value) = prefs.edit().putBoolean("fcm_reminders_enabled", value).apply()

    var fcmToken: String?
        get() = prefs.getString("fcm_device_token", null)
        set(value) = prefs.edit().putString("fcm_device_token", value).apply()

    var lastFcmNotificationBody: String?
        get() = prefs.getString("last_fcm_notification_body", null)
        set(value) = prefs.edit().putString("last_fcm_notification_body", value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean("is_dark_mode", false)
        set(value) = prefs.edit().putBoolean("is_dark_mode", value).apply()

    // Student Preferred Dining Hall (سلف انتخابی دانشجو)
    var preferredSelfName: String?
        get() = prefs.getString("preferred_self_name", null)?.takeIf { it.isNotBlank() }
        set(value) = prefs.edit().putString("preferred_self_name", value).apply()

    var notifyDifferentSelf: Boolean
        get() = prefs.getBoolean("notify_different_self", true)
        set(value) = prefs.edit().putBoolean("notify_different_self", value).apply()

    var knownSelfNames: Set<String>
        get() = prefs.getStringSet("known_self_names", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("known_self_names", value).apply()

    // GitHub Auto-Update preferences
    var githubRepo: String
        get() {
            val saved = prefs.getString("github_repo", null)
            return if (saved.isNullOrBlank() || saved == "mr-alirezaw/samad-food-qr") {
                "valiolla77-wq/samadQR"
            } else {
                saved
            }
        }
        set(value) = prefs.edit().putString("github_repo", value.trim()).apply()

    var autoCheckUpdates: Boolean
        get() = prefs.getBoolean("auto_check_updates", true)
        set(value) = prefs.edit().putBoolean("auto_check_updates", value).apply()

    var lastUpdateCheckTimestamp: Long
        get() = prefs.getLong("last_update_check_ts", 0L)
        set(value) = prefs.edit().putLong("last_update_check_ts", value).apply()

    // Cafeteria active meals (filter out meals not served by university cafeteria)
    var cafeteriaBreakfastEnabled: Boolean
        get() = prefs.getBoolean("cafeteria_breakfast_enabled", false)
        set(value) = prefs.edit().putBoolean("cafeteria_breakfast_enabled", value).apply()

    var cafeteriaLunchEnabled: Boolean
        get() = prefs.getBoolean("cafeteria_lunch_enabled", true)
        set(value) = prefs.edit().putBoolean("cafeteria_lunch_enabled", value).apply()

    var cafeteriaDinnerEnabled: Boolean
        get() = prefs.getBoolean("cafeteria_dinner_enabled", true)
        set(value) = prefs.edit().putBoolean("cafeteria_dinner_enabled", value).apply()

    var cafeteriaSuhurEnabled: Boolean
        get() = prefs.getBoolean("cafeteria_suhur_enabled", false)
        set(value) = prefs.edit().putBoolean("cafeteria_suhur_enabled", value).apply()

    var cafeteriaIftarEnabled: Boolean
        get() = prefs.getBoolean("cafeteria_iftar_enabled", false)
        set(value) = prefs.edit().putBoolean("cafeteria_iftar_enabled", value).apply()

    // Serving hours (HH:mm)
    var breakfastStart: String
        get() = prefs.getString("meal_breakfast_start", "06:30") ?: "06:30"
        set(value) = prefs.edit().putString("meal_breakfast_start", value).apply()

    var breakfastEnd: String
        get() = prefs.getString("meal_breakfast_end", "08:30") ?: "08:30"
        set(value) = prefs.edit().putString("meal_breakfast_end", value).apply()

    var lunchStart: String
        get() = prefs.getString("meal_lunch_start", "11:30") ?: "11:30"
        set(value) = prefs.edit().putString("meal_lunch_start", value).apply()

    var lunchEnd: String
        get() = prefs.getString("meal_lunch_end", "15:00") ?: "15:00"
        set(value) = prefs.edit().putString("meal_lunch_end", value).apply()

    var dinnerStart: String
        get() = prefs.getString("meal_dinner_start", "18:30") ?: "18:30"
        set(value) = prefs.edit().putString("meal_dinner_start", value).apply()

    var dinnerEnd: String
        get() = prefs.getString("meal_dinner_end", "21:30") ?: "21:30"
        set(value) = prefs.edit().putString("meal_dinner_end", value).apply()

    var iftarStart: String
        get() = prefs.getString("meal_iftar_start", "17:30") ?: "17:30"
        set(value) = prefs.edit().putString("meal_iftar_start", value).apply()

    var iftarEnd: String
        get() = prefs.getString("meal_iftar_end", "18:30") ?: "18:30"
        set(value) = prefs.edit().putString("meal_iftar_end", value).apply()

    var suhurStart: String
        get() = prefs.getString("meal_suhur_start", "03:30") ?: "03:30"
        set(value) = prefs.edit().putString("meal_suhur_start", value).apply()

    var suhurEnd: String
        get() = prefs.getString("meal_suhur_end", "05:00") ?: "05:00"
        set(value) = prefs.edit().putString("meal_suhur_end", value).apply()

    var messengerUserId: String
        get() {
            var id = prefs.getString(KEY_MESSENGER_USER_ID, null)
            if (id.isNullOrBlank()) {
                val randNum = (10000..99999).random()
                id = "SMD-$randNum"
                prefs.edit().putString(KEY_MESSENGER_USER_ID, id).apply()
            }
            return id
        }
        set(value) = prefs.edit().putString(KEY_MESSENGER_USER_ID, value.trim()).apply()

    var messengerDisplayName: String
        get() = prefs.getString(KEY_MESSENGER_DISPLAY_NAME, null)
            ?: studentName
            ?: "دانشجو"
        set(value) = prefs.edit().putString(KEY_MESSENGER_DISPLAY_NAME, value.trim()).apply()

    var messengerCustomUsername: String
        get() = prefs.getString(KEY_MESSENGER_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MESSENGER_USERNAME, value.trim().removePrefix("@")).apply()

    var messengerRelayServer: String
        get() = prefs.getString(KEY_MESSENGER_RELAY_SERVER, "cloudflare") ?: "cloudflare"
        set(value) = prefs.edit().putString(KEY_MESSENGER_RELAY_SERVER, value).apply()

    fun getEffectiveMessengerHandle(): String {
        val custom = messengerCustomUsername
        return if (custom.isNotBlank()) "@$custom" else messengerUserId
    }

    var biometricGateEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_GATE, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_GATE, value).apply()

    var friendReserveFeatureEnabled: Boolean
        get() = prefs.getBoolean(KEY_FRIEND_RESERVE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_FRIEND_RESERVE_ENABLED, value).apply()

    var deviceAuthSecret: String?
        get() = prefs.getString(KEY_DEVICE_AUTH_SECRET, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_AUTH_SECRET, value).apply()

    var deviceRegisteredId: String?
        get() = prefs.getString(KEY_DEVICE_REGISTERED_ID, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_REGISTERED_ID, value).apply()

    fun isTokenValid(): Boolean {
        val token = accessToken
        if (token.isNullOrBlank()) return false
        val expiry = tokenExpiryTimestamp
        return expiry == 0L || System.currentTimeMillis() < expiry
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://saba.tvu.ac.ir/"
        private const val PREFS_FILE_NAME = "secure_samad_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMEMBER_PW = "remember_password"
        private const val KEY_BIOMETRIC_GATE = "biometric_gate_enabled"
        private const val KEY_FRIEND_RESERVE_ENABLED = "friend_reserve_feature_enabled"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_LAST_SYNC = "last_sync"
        private const val KEY_STUDENT_NAME = "student_name"
        private const val KEY_STUDENT_NUM = "student_number"
        private const val KEY_STUDENT_MAJOR = "student_major"
        private const val KEY_AUTO_SYNC = "auto_sync_daily"
        private const val KEY_REMINDER_NOTIFICATION = "meal_reminder_notification"
        private const val KEY_MESSENGER_USER_ID = "messenger_user_id"
        private const val KEY_MESSENGER_DISPLAY_NAME = "messenger_display_name"
        private const val KEY_MESSENGER_USERNAME = "messenger_username"
        private const val KEY_MESSENGER_RELAY_SERVER = "messenger_relay_server"
        private const val KEY_DEVICE_AUTH_SECRET = "device_auth_secret"
        private const val KEY_DEVICE_REGISTERED_ID = "device_registered_id"

        private fun createEncryptedPrefs(appContext: Context): SharedPreferences {
            // Delete old plaintext fallback prefs file if it existed
            try {
                val fallbackFile = java.io.File(appContext.filesDir.parentFile, "shared_prefs/samad_prefs_fallback.xml")
                if (fallbackFile.exists()) {
                    fallbackFile.delete()
                }
            } catch (_: Exception) {}

            return try {
                buildEncryptedSharedPreferences(appContext)
            } catch (e: Exception) {
                // Keyset or encrypted prefs file was corrupted. Purge corrupted files and recreate cleanly.
                purgeCorruptedPrefsFiles(appContext)
                try {
                    buildEncryptedSharedPreferences(appContext)
                } catch (e2: Exception) {
                    throw SecurityException("Secure storage initialization failed; user re-login required", e2)
                }
            }
        }

        private fun buildEncryptedSharedPreferences(appContext: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                appContext,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private fun purgeCorruptedPrefsFiles(appContext: Context) {
            try {
                val sharedPrefsDir = java.io.File(appContext.filesDir.parentFile, "shared_prefs")
                if (sharedPrefsDir.exists() && sharedPrefsDir.isDirectory) {
                    sharedPrefsDir.listFiles()?.forEach { file ->
                        if (file.name.contains(PREFS_FILE_NAME) || file.name.contains("keyset") || file.name.contains("samad_prefs")) {
                            file.delete()
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
