package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CachedMealTypeEntity
import com.example.data.local.entity.CachedReserveEntity
import com.example.data.model.MealTypeInfo
import com.example.data.model.Reserve
import com.example.data.remote.ApiClient
import com.example.data.remote.ExtractedForgotCode
import com.example.data.remote.ForgotCodeExtractor
import com.example.data.remote.SamadApiService
import com.example.data.security.SecurePrefs
import com.example.notification.NotificationHelper
import com.example.util.MealTimeHelper
import com.example.util.PersianDateUtil
import com.example.widget.SamadFoodWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class SyncResult {
    data class Success(val reservesCount: Int, val weekDate: String, val isOffline: Boolean = false) : SyncResult()
    data class NoReservesFound(val checkedWeeks: Int) : SyncResult()
    data class Error(val message: String, val isAuthError: Boolean = false) : SyncResult()
}

class SamadRepository(
    private val context: Context,
    val prefs: SecurePrefs = SecurePrefs(context),
    val db: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dao = db.reserveDao()

    private val reLoginLock = Any()

    /**
     * Synchronous re-login callback invoked by ApiClient.tokenAuthenticator on HTTP 401
     */
    fun reLoginSync(): String? {
        synchronized(reLoginLock) {
            val username = prefs.username
            val password = prefs.password
            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                Log.w("SamadRepository", "reLoginSync: No stored credentials available for auto-re-login")
                return null
            }
            return try {
                val api = getApiService()
                val tokenResp = kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    api.login(
                        username = username.trim(),
                        password = password.trim()
                    )
                }
                val token = tokenResp.accessToken
                if (token.isNotBlank()) {
                    prefs.accessToken = token
                    val expSec = tokenResp.expiresIn
                    val expiryMs = if (expSec > 0) {
                        System.currentTimeMillis() + (expSec * 1000L) - 30_000L
                    } else {
                        System.currentTimeMillis() + (2L * 3600 * 1000L) - 30_000L
                    }
                    prefs.tokenExpiryTimestamp = expiryMs
                    Log.d("SamadRepository", "reLoginSync: Successfully renewed bearer token via Authenticator")
                    token
                } else null
            } catch (e: Exception) {
                Log.e("SamadRepository", "reLoginSync failed: ${e.message}")
                null
            }
        }
    }

    private fun getApiService(): SamadApiService {
        return ApiClient.createService(
            baseUrl = prefs.baseUrl,
            tokenProvider = { prefs.accessToken },
            reLoginProvider = { reLoginSync() }
        )
    }

    fun getAllCachedReserves(): Flow<List<CachedReserveEntity>> = dao.getAllReserves()

    fun getAllCachedMealTypes(): Flow<List<CachedMealTypeEntity>> = dao.getAllMealTypes()

    private fun normalizeDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                in '۰'..'۹' -> sb.append(('0'.code + (ch.code - '۰'.code)).toChar())
                in '٠'..'٩' -> sb.append(('0'.code + (ch.code - '٠'.code)).toChar())
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Persian/Arabic keyboards type passwords with ۰-۹ / ٠-٩ digits. The Samad server
     * compares the raw string, so such a password is always rejected with invalid_grant.
     * Only convert when the whole password is made of Persian/Arabic digits, so real
     * passwords that intentionally contain those characters stay untouched.
     */
    private fun normalizeDigitOnlyPassword(input: String): String {
        val converted = normalizeDigits(input)
        return if (converted != input && converted.isNotEmpty() && converted.all { it in '0'..'9' }) {
            converted
        } else {
            input
        }
    }

    /** Username cleanup: digits, invisible RTL marks and stray whitespace. */
    private fun normalizeUsername(input: String): String {
        return normalizeDigits(input)
            .replace("\u200c", "")
            .replace("\u200e", "")
            .replace("\u200f", "")
            .replace("\u202a", "")
            .replace("\u202b", "")
            .replace("\u202c", "")
            .trim()
    }

    suspend fun login(usernameInput: String, passwordInput: String, remember: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanUser = normalizeUsername(usernameInput)
            val typedPass = passwordInput.trim()
            val api = getApiService()
            var usedPass = typedPass
            val tokenResp = try {
                api.login(username = cleanUser, password = typedPass)
            } catch (e: retrofit2.HttpException) {
                // Persian-digit password fallback: retry once with Latin digits
                val retryPass = normalizeDigitOnlyPassword(typedPass)
                if ((e.code() == 400 || e.code() == 401) && retryPass != typedPass) {
                    usedPass = retryPass
                    api.login(username = cleanUser, password = retryPass)
                } else {
                    throw e
                }
            }

            val token = tokenResp.accessToken
            prefs.accessToken = token
            // expires_in in seconds -> convert to ms with buffer
            val expSec = tokenResp.expiresIn
            val expiryMs = if (expSec > 0) {
                System.currentTimeMillis() + (expSec * 1000L) - 30_000L
            } else {
                System.currentTimeMillis() + (2L * 3600 * 1000L) - 30_000L
            }
            prefs.tokenExpiryTimestamp = expiryMs
            prefs.username = cleanUser
            prefs.rememberPassword = remember
            if (remember) {
                prefs.password = usedPass
            } else {
                prefs.password = null
            }

            // Fetch user info
            try {
                val userResp = api.getUserInfo("Bearer $token")
                userResp.payload?.let { user ->
                    val fullName = listOfNotNull(user.firstName, user.lastName).joinToString(" ").trim()
                    prefs.studentName = fullName.ifBlank { user.username }
                    prefs.studentNumber = user.student?.studentNumber ?: user.username
                    prefs.studentMajor = user.student?.studyMajor?.name
                }
            } catch (e: Exception) {
                Log.w("SamadRepository", "Could not fetch user profile: ${e.message}")
            }

            Result.success(token)
        } catch (e: Exception) {
            Log.e("SamadRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    fun formatLoginErrorMessage(e: Throwable?): String {
        if (e == null) {
            return "ورود ناموفق بود. لطفاً اتصال اینترنت و اطلاعات حساب خود را بررسی کنید."
        }
        val baseUrl = prefs.baseUrl.removeSuffix("/")
        if (e is retrofit2.HttpException) {
            val code = e.code()
            val errBody = try { e.response()?.errorBody()?.string() ?: "" } catch (_: Throwable) { "" }
            return when {
                code == 400 || code == 401 || errBody.contains("invalid_grant", ignoreCase = true) ||
                    errBody.contains("Bad credentials", ignoreCase = true) ->
                    "شماره دانشجویی یا رمز عبور اشتباه است."
                code == 403 ->
                    "دسترسی توسط سرور سماد مسدود شده است (کد ۴۰۳)."
                code == 404 ->
                    "آدرس ورود در سرور ($baseUrl) یافت نشد (کد ۴۰۴). آدرس سرور را بررسی کنید."
                code >= 500 ->
                    "سرور سامانه سماد موقتاً دچار اختلال است (کد $code)."
                else ->
                    "خطا در ورود به سامانه (کد $code)."
            }
        }
        return when {
            e is java.net.SocketTimeoutException ->
                "اتصال به سرور سماد ($baseUrl) به پایان زمان رسید (Timeout). دسترسی شبکه فعلی به سرور دانشگاه ممکن است مسدود باشد یا سرور پاسخ نمی‌دهد."
            e is java.net.UnknownHostException ->
                "سرور سماد ($baseUrl) یافت نشد. اتصال اینترنت یا آدرس سرور را بررسی کنید."
            e is java.net.ConnectException ->
                "امکان برقراری ارتباط با سرور سماد ($baseUrl) وجود ندارد."
            e is javax.net.ssl.SSLException || e is java.security.cert.CertificateException -> {
                val causes = generateSequence(e as Throwable?) { it.cause }.toList()
                val detail = when {
                    causes.any { it is java.security.cert.CertificateExpiredException } ->
                        "تاریخ گواهی سرور منقضی شده است."
                    causes.any { it is java.security.cert.CertificateNotYetValidException } ->
                        "تاریخ/ساعت گوشی با گواهی سرور هم‌خوان نیست؛ ساعت دستگاه را درست کنید."
                    causes.any { it.message?.contains("Trust anchor", ignoreCase = true) == true } ->
                        "زنجیره‌ی گواهی سرور روی این گوشی شناخته نمی‌شود (گواهی ریشه نصب نیست)."
                    causes.any { it is javax.net.ssl.SSLPeerUnverifiedException } ->
                        "گواهی با دامنه‌ی $baseUrl مطابقت ندارد؛ احتمالاً VPN/پروکسی ارتباط را رمزگشایی می‌کند."
                    else -> e.message ?: "خطای نامشخص TLS."
                }
                "خطای امنیتی اتصال (SSL) به $baseUrl: $detail"
            }
            else -> {
                val msg = e.localizedMessage?.takeIf { it.isNotBlank() }
                if (msg != null) "خطای ورود: $msg" else "ورود ناموفق بود. لطفاً مجدداً تلاش کنید."
            }
        }
    }

    suspend fun ensureValidToken(forceRefresh: Boolean = false): String? = withContext(Dispatchers.IO) {
        val currentToken = prefs.accessToken
        if (!forceRefresh && prefs.isTokenValid() && !currentToken.isNullOrBlank()) {
            return@withContext currentToken
        }

        prefs.invalidateToken()

        // Try re-login if credentials are saved
        val username = prefs.username
        val password = prefs.password
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            val loginResult = login(username, password, prefs.rememberPassword)
            return@withContext loginResult.getOrNull()
        }

        null
    }

    fun formatErrorMessage(e: Throwable): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("401") || (e is retrofit2.HttpException && e.code() == 401) ->
                "نشست کاربری شما در سامانه سماد منقضی شده است. لطفاً مجدداً وارد حساب خود شوید."
            msg.contains("404") || (e is retrofit2.HttpException && e.code() == 404) ->
                "اطلاعات مورد نظر در سامانه سماد یافت نشد."
            msg.contains("Unable to resolve host") || msg.contains("ConnectException") || msg.contains("SocketTimeoutException") ->
                "خطا در برقراری ارتباط با سرور سماد. لطفاً وضعیت اینترنت خود را بررسی نمایید."
            else -> e.localizedMessage ?: "خطای ناشناخته در ارتباط با سرور سماد"
        }
    }

    /**
     * Checks up to 3 weeks:
     * 1. Current week
     * 2. If no reserves, next week
     * 3. If no reserves, week after that
     * 4. If none, returns NoReservesFound
     */
    suspend fun fetchAndCacheReserves(): SyncResult = withContext(Dispatchers.IO) {
        val token = ensureValidToken()
        if (token.isNullOrBlank()) {
            return@withContext SyncResult.Error(
                message = "لطفاً ابتدا وارد حساب کاربری خود شوید.",
                isAuthError = true
            )
        }

        val bearer = "Bearer $token"
        val api = getApiService()

        val today = LocalDate.now()
        val daysSinceSaturday = (today.dayOfWeek.value - 6 + 7) % 7
        val currentSaturday = today.minusDays(daysSinceSaturday.toLong())

        val allFoundReserves = mutableListOf<CachedReserveEntity>()
        val allFoundMealTypes = mutableMapOf<Int, MealTypeInfo>()
        var foundWeekStr = ""

        var anyWeekSuccess = false
        var lastNetworkErrorMsg: String? = null

        try {
            // Check current week and next 3 upcoming weeks (all available reserves across weeks)
            for (weekOffset in 0 until 4) {
                val targetDate = today.plusWeeks(weekOffset.toLong())
                val weekStartDateStr = PersianDateUtil.getWeekStart(targetDate)

                Log.d("SamadRepository", "Checking week offset $weekOffset with date: $weekStartDateStr")
                try {
                    val response = api.getReserves(
                        bearer = bearer,
                        weekStart = weekStartDateStr
                    )
                    anyWeekSuccess = true

                    val payload = response.payload
                    val weekDays = payload?.weekDays ?: emptyList()
                    val mealTypes = payload?.mealTypes ?: emptyList()
                    for (mt in mealTypes) {
                        allFoundMealTypes[mt.id] = mt
                    }

                    val existingCodesMap = try {
                        dao.getAllReservesDirect().associate { it.reserveId to (it.forgotCardCode to it.codeValid) }
                    } catch (e: Exception) {
                        emptyMap()
                    }

                    for (day in weekDays) {
                        for (slot in day.mealTypes) {
                            val candidateReserves = mutableListOf<Reserve>()
                            slot.reserve?.let { candidateReserves.add(it) }
                            slot.reserves?.let { candidateReserves.addAll(it) }

                            for (r in candidateReserves) {
                                val reserveId = r.id ?: 0L
                                if (reserveId == 0L && (r.programId ?: 0L) == 0L) continue
                                if (r.foodNames.isBlank()) continue

                                // Definite negatives (unreserved / unselected by student in this self)
                                if (r.selected == false) continue
                                if (r.isReserved == false) continue
                                if (r.hasReserve == false) continue
                                if (r.reserved == false) continue
                                if (slot.selected == false) continue
                                if (slot.isReserved == false) continue
                                if (slot.hasReserve == false) continue

                                // If explicitly marked as canReserve=true and canCancel=false (not yet booked)
                                if (r.canReserve == true && r.canCancel == false) continue

                                // If counts are explicitly zero
                                val countsZero = (r.selectedCount != null && r.selectedCount <= 0) &&
                                    (r.remainedCount != null && r.remainedCount <= 0) &&
                                    (r.count != null && r.count <= 0)
                                if (countsZero) continue

                                // Strict confirmation check from fresh API response
                                val hasDirectCode = !r.forgetCardCode.isNullOrBlank() ||
                                    !r.forgotCardCode.isNullOrBlank() ||
                                    !r.barcode.isNullOrBlank() ||
                                    !r.printCode.isNullOrBlank()

                                val isConfirmedByApi = hasDirectCode ||
                                    r.selected == true ||
                                    slot.selected == true ||
                                    r.isReserved == true ||
                                    slot.isReserved == true ||
                                    r.hasReserve == true ||
                                    slot.hasReserve == true ||
                                    r.reserved == true ||
                                    r.canCancel == true ||
                                    r.consumed ||
                                    ((r.selectedCount ?: 0) > 0) ||
                                    ((r.count ?: 0) > 0)

                                val isExplicitlyNotBooked = r.selected == false ||
                                    slot.selected == false ||
                                    r.isReserved == false ||
                                    slot.isReserved == false ||
                                    r.hasReserve == false ||
                                    slot.hasReserve == false ||
                                    r.reserved == false ||
                                    (r.canReserve == true && r.canCancel == false)

                                if (!isConfirmedByApi || isExplicitlyNotBooked) {
                                    Log.d("SamadRepository", "Skipping unreserved dining hall: ${r.selfName} - ${r.foodNames}")
                                    continue
                                }

                                var forgotCode: String? = r.forgetCardCode?.takeIf { it.isNotBlank() }
                                    ?: r.forgotCardCode?.takeIf { it.isNotBlank() }
                                    ?: r.barcode?.takeIf { it.isNotBlank() }
                                    ?: r.printCode?.takeIf { it.isNotBlank() }

                                var codeValid = true

                                // If forgotCode is blank and reserveId is valid, attempt fetching code from API
                                if (forgotCode.isNullOrBlank() && reserveId != 0L) {
                                    try {
                                        val codeResp = api.getForgotCardCode(
                                            bearer = bearer,
                                            reserveId = reserveId,
                                            count = 1,
                                            dailySale = false
                                        )
                                        val p = codeResp.payload
                                        if (p != null && !p.forgotCardCode.isNullOrBlank()) {
                                            forgotCode = p.forgotCardCode
                                            codeValid = p.valid
                                        }
                                    } catch (e: Exception) {
                                        Log.w("SamadRepository", "Failed to fetch code for reserve $reserveId: ${e.message}")
                                    }
                                }

                                // Fallback to offline cached code only for this confirmed reservation
                                if (forgotCode.isNullOrBlank()) {
                                    val existing = existingCodesMap[reserveId]
                                    if (!existing?.first.isNullOrBlank()) {
                                        forgotCode = existing?.first
                                        codeValid = existing?.second ?: true
                                    }
                                }

                                val isConsumed = r.consumed || ((r.remainedCount ?: 1) <= 0 && (r.selectedCount ?: 1) > 0)
                                val entity = CachedReserveEntity(
                                    reserveId = reserveId,
                                    programId = r.programId ?: 0L,
                                    date = day.date,
                                    dateJStr = day.dateJStr.ifBlank { r.programDateStr },
                                    dayName = day.dayTranslated.ifBlank { day.day },
                                    mealTypeId = slot.mealTypeId,
                                    mealName = slot.name,
                                    foodName = r.foodNames,
                                    besideFoodNames = r.besideFoodNames,
                                    selfName = r.selfName,
                                    selfCode = r.selfCode,
                                    price = r.price,
                                    consumed = isConsumed,
                                    forgotCardCode = forgotCode,
                                    codeValid = codeValid,
                                    isGuest = false,
                                    syncTimestamp = System.currentTimeMillis()
                                )
                                allFoundReserves.add(entity)
                            }
                        }
                    }

                    if (allFoundReserves.isNotEmpty() && foundWeekStr.isEmpty()) {
                        foundWeekStr = weekStartDateStr
                    }
                } catch (e: Exception) {
                    lastNetworkErrorMsg = e.localizedMessage ?: e.message
                    Log.w("SamadRepository", "Failed checking week $weekOffset: ${e.message}")
                }
            }

            // CRITICAL: If no week request succeeded (server down, network off, etc.), preserve existing cache!
            if (!anyWeekSuccess) {
                return@withContext SyncResult.Error(
                    message = "سامانه سماد در دسترس نیست. جهت حفظ بارکدها و اطلاعات شما، حافظه ذخیره‌شده دست‌نخورده باقی ماند."
                )
            }

            // Save detected dining hall names for user cafeteria selection
            val distinctSelfs = allFoundReserves.map { it.selfName.trim() }.filter { it.isNotBlank() }.toSet()
            if (distinctSelfs.isNotEmpty()) {
                prefs.knownSelfNames = prefs.knownSelfNames + distinctSelfs
            }

            // Only clear and update when fresh valid data was actually retrieved from the server
            if (allFoundReserves.isNotEmpty()) {
                dao.clearNonGuestReserves()
                dao.insertReserves(allFoundReserves)

                if (allFoundMealTypes.isNotEmpty()) {
                    dao.clearMealTypes()
                    val mealEntities = allFoundMealTypes.values.map {
                        CachedMealTypeEntity(
                            id = it.id,
                            name = it.name,
                            disPriority = it.disPriority
                        )
                    }
                    dao.insertMealTypes(mealEntities)
                }

                prefs.lastSyncTimestamp = System.currentTimeMillis()

                scheduleReminders(allFoundReserves)
                SamadFoodWidgetProvider.updateAllWidgets(context)

                SyncResult.Success(
                    reservesCount = allFoundReserves.size,
                    weekDate = foundWeekStr.ifBlank { PersianDateUtil.getWeekStart(today) },
                    isOffline = false
                )
            } else {
                // University server confirmed 0 active reserves. Safely clean non-guest cache so ghost/cancelled reserves vanish.
                dao.clearNonGuestReserves()
                prefs.lastSyncTimestamp = System.currentTimeMillis()
                SamadFoodWidgetProvider.updateAllWidgets(context)
                cleanupSensitiveOldData()
                SyncResult.NoReservesFound(checkedWeeks = 4)
            }

        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SamadRepository", "Fetch reserves failed: ${e.message}")
            }
            SyncResult.Error(
                message = e.localizedMessage ?: "اتصال به سامانه برقرار نشد"
            )
        }
    }

    suspend fun insertSingleReserve(reserve: CachedReserveEntity) = withContext(Dispatchers.IO) {
        dao.insertReserve(reserve)
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun deleteReserve(reserveId: Long) = withContext(Dispatchers.IO) {
        dao.deleteReserve(reserveId)
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun fetchForgotCodeSingle(reserveId: Long): ExtractedForgotCode = withContext(Dispatchers.IO) {
        val token = ensureValidToken() ?: return@withContext ExtractedForgotCode(null, message = "لطفاً ابتدا وارد حساب کاربری شوید")
        val api = getApiService()
        val bearer = "Bearer $token"

        try {
            val response = api.getForgotCardCode(
                bearer = bearer,
                reserveId = reserveId,
                count = 1,
                dailySale = false
            )
            val p = response.payload
            val code = p?.forgotCardCode
            val valid = p?.valid ?: true

            if (!code.isNullOrBlank()) {
                dao.updateCode(reserveId, code, valid)
                SamadFoodWidgetProvider.updateAllWidgets(context)
                ExtractedForgotCode(code = code, isValid = valid)
            } else {
                val msg = if (!valid) "کد هنوز معتبر نیست (خارج از بازه سرو)" else "کد فراموشی برای این رزرو یافت نشد"
                ExtractedForgotCode(code = null, isValid = valid, message = msg)
            }
        } catch (e: retrofit2.HttpException) {
            val errBody = e.response()?.errorBody()?.string()
            if (com.example.BuildConfig.DEBUG) {
                Log.w("SamadRepository", "HTTP error ${e.code()}")
            }
            val extracted = ForgotCodeExtractor.extract(errBody)
            val msg = if (!extracted.message.isNullOrBlank()) extracted.message else "خطای سامانه (${e.code()})"
            ExtractedForgotCode(null, message = msg)
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SamadRepository", "getForgotCardCode failed for reserve $reserveId")
            }
            ExtractedForgotCode(null, message = e.localizedMessage ?: "خطا در دریافت کد")
        }
    }

    suspend fun setManualReserveCode(reserveId: Long, code: String) = withContext(Dispatchers.IO) {
        dao.updateCode(reserveId, code.trim(), true)
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun setReserveConsumed(reserveId: Long, consumed: Boolean) = withContext(Dispatchers.IO) {
        dao.updateConsumedStatus(reserveId, consumed)
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun fetchFriendReserveForDate(
        friendUsername: String,
        friendPassword: String,
        targetDate: String,
        targetMealTypeId: Int? = null
    ): Result<CachedReserveEntity> = withContext(Dispatchers.IO) {
        try {
            if (friendUsername.isBlank() || friendPassword.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("نام کاربری و رمز عبور الزامی است"))
            }

            val api = getApiService()
            val tokenResp = try {
                api.login(username = friendUsername.trim(), password = friendPassword.trim())
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("ورود ناموفق: نام کاربری یا رمز عبور اشتباه است یا سامانه پاسخ نداد"))
            }

            val accessToken = tokenResp.accessToken
            if (accessToken.isNullOrBlank()) {
                return@withContext Result.failure(Exception("توکن دسترسی دریافت نشد"))
            }

            val friendBearer = "Bearer $accessToken"

            // Get student display name
            val friendName = try {
                val userResp = api.getUserInfo(friendBearer)
                val p = userResp.payload
                val full = listOfNotNull(p?.firstName, p?.lastName).joinToString(" ").trim()
                if (full.isNotBlank()) full else (p?.username ?: friendUsername.trim())
            } catch (e: Exception) {
                friendUsername.trim()
            }

            // Target date & week start
            val localDate = try { LocalDate.parse(targetDate) } catch (e: Exception) { LocalDate.now() }
            val weekStartDateStr = PersianDateUtil.getWeekStart(localDate)

            val reservesResp = api.getReserves(
                bearer = friendBearer,
                weekStart = weekStartDateStr
            )

            val weekDays = reservesResp.payload?.weekDays ?: emptyList()
            val targetDay = weekDays.find { it.date == targetDate }
                ?: return@withContext Result.failure(Exception("در تاریخ ${PersianDateUtil.getDayOfWeekPersian(localDate)} رزروی برای این دانشجو یافت نشد"))

            fun isBookingConfirmed(slot: com.example.data.model.MealSlot, r: Reserve?): Boolean {
                if (r == null) return false
                val id = r.id ?: 0L
                if (id == 0L && (r.programId ?: 0L) == 0L) return false
                if (r.foodNames.isBlank()) return false
                if (r.selected == false || r.isReserved == false || r.hasReserve == false || r.reserved == false) return false
                if (slot.selected == false || slot.isReserved == false || slot.hasReserve == false) return false
                if (r.canReserve == true && r.canCancel == false) return false
                val countsZero = (r.selectedCount != null && r.selectedCount <= 0) &&
                    (r.remainedCount != null && r.remainedCount <= 0) &&
                    (r.count != null && r.count <= 0)
                if (countsZero) return false
                return r.selected == true ||
                    r.isReserved == true ||
                    r.hasReserve == true ||
                    r.reserved == true ||
                    slot.hasReserve == true ||
                    slot.isReserved == true ||
                    r.canCancel == true ||
                    (r.selectedCount != null && r.selectedCount > 0) ||
                    (r.remainedCount != null && r.remainedCount > 0) ||
                    (r.count != null && r.count > 0) ||
                    r.consumed ||
                    !r.forgetCardCode.isNullOrBlank() ||
                    !r.forgotCardCode.isNullOrBlank() ||
                    !r.barcode.isNullOrBlank() ||
                    !r.printCode.isNullOrBlank()
            }

            // Find matching slot with active booking
            val matchingSlot = if (targetMealTypeId != null && targetMealTypeId != 0) {
                targetDay.mealTypes.find {
                    it.mealTypeId == targetMealTypeId && isBookingConfirmed(it, it.reserve)
                } ?: targetDay.mealTypes.find {
                    isBookingConfirmed(it, it.reserve)
                }
            } else {
                targetDay.mealTypes.find {
                    isBookingConfirmed(it, it.reserve)
                }
            }

            val reserve = matchingSlot?.reserve
                ?: return@withContext Result.failure(Exception("برای تاریخ ${PersianDateUtil.getDayOfWeekPersian(localDate)} در این وعده رزروی ثبت نشده است"))

            val reserveId = reserve.id ?: 0L

            // Fetch forgot card code if not directly provided
            var forgotCode: String? = reserve.forgetCardCode?.takeIf { it.isNotBlank() }
                ?: reserve.forgotCardCode?.takeIf { it.isNotBlank() }
                ?: reserve.barcode?.takeIf { it.isNotBlank() }
                ?: reserve.printCode?.takeIf { it.isNotBlank() }

            if (forgotCode.isNullOrBlank() && reserveId != 0L) {
                try {
                    val codeResp = api.getForgotCardCode(
                        bearer = friendBearer,
                        reserveId = reserveId,
                        count = 1,
                        dailySale = false
                    )
                    val p = codeResp.payload
                    if (p != null && !p.forgotCardCode.isNullOrBlank()) {
                        forgotCode = p.forgotCardCode
                    }
                } catch (e: Exception) {
                    Log.w("SamadRepository", "Could not fetch code for friend reserve: ${e.message}")
                }
            }

            val isConsumed = reserve.consumed || ((reserve.remainedCount ?: 1) <= 0 && (reserve.selectedCount ?: 1) > 0)
            val pDate = PersianDateUtil.gregorianToPersian(localDate)
            val dateJStr = targetDay.dateJStr.ifBlank { "${pDate.year}/${pDate.month}/${pDate.day}" }

            val entity = CachedReserveEntity(
                reserveId = reserveId,
                programId = reserve.programId ?: 0L,
                date = targetDate,
                dateJStr = dateJStr,
                dayName = targetDay.dayTranslated.ifBlank { PersianDateUtil.getDayOfWeekPersian(localDate) },
                mealTypeId = matchingSlot.mealTypeId,
                mealName = matchingSlot.name,
                foodName = reserve.foodNames,
                besideFoodNames = reserve.besideFoodNames,
                selfName = reserve.selfName,
                selfCode = reserve.selfCode,
                price = reserve.price,
                consumed = isConsumed,
                forgotCardCode = forgotCode,
                codeValid = true,
                isGuest = true,
                guestOwnerName = friendName,
                syncTimestamp = System.currentTimeMillis()
            )

            dao.insertReserve(entity)
            SamadFoodWidgetProvider.updateAllWidgets(context)

            Result.success(entity)
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e("SamadRepository", "fetchFriendReserveForDate failed")
            }
            Result.failure(e)
        }
    }

    suspend fun cleanupSensitiveOldData() = withContext(Dispatchers.IO) {
        try {
            val twoDaysAgo = java.time.LocalDate.now().minusDays(2).toString()
            val twoDaysAgoMillis = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000)
            dao.deleteExpiredGuestReserves(twoDaysAgo)
            db.chatDao().deleteOldOrClaimedMessages(twoDaysAgoMillis)
        } catch (_: Exception) {}
    }

    private fun scheduleReminders(reserves: List<CachedReserveEntity>) {
        if (!prefs.mealReminderNotificationEnabled) return

        val now = LocalDateTime.now()
        for (reserve in reserves) {
            val reminderTime = MealTimeHelper.getReminderTargetDateTime(
                dateStr = reserve.date,
                mealName = reserve.mealName,
                prefs = prefs
            )

            // If reminder is within next 24 hours and in the future
            if (reminderTime.isAfter(now) && reminderTime.isBefore(now.plusDays(7))) {
                val window = MealTimeHelper.getServingWindow(
                    MealTimeHelper.MealCategory.fromName(reserve.mealName),
                    prefs
                )
                // Schedule reminder worker
                com.example.work.WorkScheduler.scheduleMealReminder(
                    context = context,
                    reserveId = reserve.reserveId,
                    mealType = reserve.mealName,
                    servingTime = window.formatRange(),
                    foodName = reserve.foodName,
                    selfName = reserve.selfName,
                    code = reserve.forgotCardCode,
                    triggerAt = reminderTime
                )
            }
        }
    }

    val pendingDao = db.pendingReserveDao()

    fun getAllPendingReserves(): Flow<List<com.example.data.local.entity.PendingReserveEntity>> =
        pendingDao.getAllPendingReserves()

    suspend fun insertPendingReserve(entity: com.example.data.local.entity.PendingReserveEntity) = withContext(Dispatchers.IO) {
        pendingDao.insertPending(entity)
    }

    suspend fun deletePendingReserve(programId: Long) = withContext(Dispatchers.IO) {
        pendingDao.deletePending(programId)
    }

    suspend fun clearAllPendingReserves() = withContext(Dispatchers.IO) {
        pendingDao.clearAll()
    }

    suspend fun fetchLiveCredit(): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val token = ensureValidToken() ?: return@withContext Result.failure(Exception("نشست کاربری منقضی شده است"))
            val api = getApiService()
            val bearer = "Bearer $token"

            // 1. Try GET /rest/users/nurture-profiles if available
            try {
                val resp = api.getNurtureProfile(bearer)
                val credit = resp.payload?.credit
                if (credit != null) {
                    return@withContext Result.success(credit)
                }
            } catch (e: Exception) {
                Log.d("SamadRepository", "getNurtureProfile not available, falling back to getReserves: ${e.message}")
            }

            // 2. Guaranteed fallback: GET /rest/reserves returns remainCredit
            val today = LocalDate.now()
            val weekStartStr = PersianDateUtil.getWeekStart(today)
            val reservesResp = api.getReserves(bearer, weekStartStr)
            val credit = reservesResp.payload?.remainCredit ?: 0L
            Result.success(credit)
        } catch (e: Exception) {
            Log.e("SamadRepository", "Failed to fetch live credit", e)
            Result.failure(Exception(formatErrorMessage(e)))
        }
    }

    suspend fun fetchAvailableSelfs(): Result<List<com.example.data.model.SelfItem>> = withContext(Dispatchers.IO) {
        try {
            val token = ensureValidToken() ?: return@withContext Result.failure(Exception("نشست کاربری منقضی شده است"))
            val api = getApiService()
            val bearer = "Bearer $token"

            // 1. Try GET /rest/reservations/selfs
            try {
                val resp = api.getAvailableSelfs(bearer)
                val list = resp.payload
                if (!list.isNullOrEmpty()) {
                    return@withContext Result.success(list)
                }
            } catch (e: Exception) {
                Log.d("SamadRepository", "getAvailableSelfs not available, extracting from weekly reserves: ${e.message}")
            }

            // 2. Fallback: extract distinct self names from weekly reserves
            val today = LocalDate.now()
            val weekStartStr = PersianDateUtil.getWeekStart(today)
            val reservesResp = api.getReserves(bearer, weekStartStr)
            val weekDays = reservesResp.payload?.weekDays ?: emptyList()

            val selfNames = linkedSetOf<String>()
            for (day in weekDays) {
                for (slot in day.mealTypes) {
                    val items = slot.reserves ?: listOfNotNull(slot.reserve)
                    for (item in items) {
                        if (item.selfName.isNotBlank()) {
                            selfNames.add(item.selfName.trim())
                        }
                    }
                }
            }

            if (selfNames.isEmpty()) {
                val preferred = prefs.preferredSelfName?.takeIf { it.isNotBlank() } ?: "سلف مرکزی"
                selfNames.add(preferred)
            }

            val list = selfNames.mapIndexed { index, name ->
                com.example.data.model.SelfItem(
                    id = index + 1,
                    name = name,
                    selfType = "NORMAL"
                )
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e("SamadRepository", "Failed to fetch available selfs", e)
            val fallback = listOf(
                com.example.data.model.SelfItem(
                    id = 1,
                    name = prefs.preferredSelfName?.takeIf { it.isNotBlank() } ?: "سلف مرکزی",
                    selfType = "NORMAL"
                )
            )
            Result.success(fallback)
        }
    }

    suspend fun fetchWeeklyPrograms(selfId: Int, weekStartDate: String): Result<com.example.data.model.ReserveProgramPayload> = withContext(Dispatchers.IO) {
        try {
            val token = ensureValidToken() ?: return@withContext Result.failure(Exception("نشست کاربری منقضی شده است"))
            val api = getApiService()
            val bearer = "Bearer $token"

            // 1. Normalize weekStartDate to Saturday 00:00:00 with space as required by Samad API
            val rawDate = weekStartDate.trim().split(" ")[0].replace("/", "-")
            val targetLocalDate = try {
                LocalDate.parse(rawDate)
            } catch (_: Exception) {
                LocalDate.now()
            }
            val weekStartFormatted = PersianDateUtil.getWeekStart(targetLocalDate)

            Log.d("SamadRepository", "fetchWeeklyPrograms: calling getReserves with weekStart=$weekStartFormatted")

            // 2. Call the working core Samad endpoint: GET rest/reserves
            val reservesResp = api.getReserves(
                bearer = bearer,
                weekStart = weekStartFormatted
            )

            val payload = reservesResp.payload
            val weekDays = payload?.weekDays ?: emptyList()

            val programList = mutableListOf<com.example.data.model.ProgramFoodItem>()
            val userReservesList = mutableListOf<com.example.data.model.UserWeekReserveItem>()

            for (day in weekDays) {
                val dateStr = day.date.trim().split(" ")[0].replace("/", "-")
                val parsedDate = try { LocalDate.parse(dateStr) } catch (_: Exception) { LocalDate.now() }
                val dayName = day.dayTranslated.ifBlank { PersianDateUtil.getDayOfWeekPersian(parsedDate) }

                // Check if Breakfast is present in this day's slots
                val hasBreakfastSlot = day.mealTypes.any { it.mealTypeId == 1 || it.mealTypeId == 6 || it.name.contains("صبحانه") }
                if (!hasBreakfastSlot && prefs.cafeteriaBreakfastEnabled) {
                    val bProgId = kotlin.math.abs((dateStr.hashCode().toLong() shl 16) or 6L).coerceAtLeast(1L)
                    programList.add(
                        com.example.data.model.ProgramFoodItem(
                            programId = bProgId,
                            date = dateStr,
                            dayTranslated = dayName,
                            foodId = 0L,
                            foodName = "رزرو وعده صبحانه",
                            foodTypeId = 1,
                            mealTypeId = 6, // Breakfast
                            price = 100000L,
                            selfId = selfId.coerceAtLeast(1),
                            selfName = prefs.preferredSelfName?.takeIf { it.isNotBlank() } ?: "سلف مرکزی",
                            reserveRuleViolated = false,
                            cancelRuleViolated = false,
                            daysDifferenceWithToday = 0
                        )
                    )
                }

                for (slot in day.mealTypes) {
                    val slotReserves = slot.reserves ?: listOfNotNull(slot.reserve)
                    val slotMealName = slot.name.ifBlank { MealTimeHelper.MealCategory.getMealTypeName(slot.mealTypeId) }

                    if (slotReserves.isEmpty()) {
                        // Unreserved slot: ensure this meal (e.g. Breakfast, Lunch, Dinner) is visible and reservable!
                        val dummyProgramId = kotlin.math.abs((dateStr.hashCode().toLong() shl 16) or slot.mealTypeId.toLong()).coerceAtLeast(1L)
                        val defaultPrice = if (slot.mealTypeId == 1 || slot.mealTypeId == 6) 100000L else 150000L
                        programList.add(
                            com.example.data.model.ProgramFoodItem(
                                programId = dummyProgramId,
                                date = dateStr,
                                dayTranslated = dayName,
                                foodId = 0L,
                                foodName = "رزرو وعده $slotMealName",
                                foodTypeId = 1,
                                mealTypeId = slot.mealTypeId,
                                price = defaultPrice,
                                selfId = selfId.coerceAtLeast(1),
                                selfName = prefs.preferredSelfName?.takeIf { it.isNotBlank() } ?: "سلف مرکزی",
                                reserveRuleViolated = false,
                                cancelRuleViolated = false,
                                daysDifferenceWithToday = 0
                            )
                        )
                    } else {
                        for (r in slotReserves) {
                            val progId = r.programId?.takeIf { it != 0L } ?: r.id ?: 0L
                            if (progId == 0L && r.foodNames.isBlank()) continue

                            val foodTitle = buildString {
                                append(r.foodNames.trim().ifBlank { "وعده $slotMealName" })
                                if (!r.besideFoodNames.isNullOrBlank()) {
                                    append(" (${r.besideFoodNames.trim()})")
                                }
                            }

                            val sName = r.selfName.ifBlank { prefs.preferredSelfName?.takeIf { it.isNotBlank() } ?: "سلف مرکزی" }
                            val sId = r.selfCode.toIntOrNull() ?: (sName.hashCode().let { if (it < 0) -it else it } % 1000 + 1)

                            // CRITICAL: isBooked must ONLY be true if this specific food was actually selected/booked!
                            val isBooked = r.selected == true ||
                                    r.isReserved == true ||
                                    r.reserved == true ||
                                    r.canCancel == true ||
                                    ((r.selectedCount ?: 0) > 0) ||
                                    ((r.count ?: 0) > 0)

                            if (isBooked) {
                                userReservesList.add(
                                    com.example.data.model.UserWeekReserveItem(
                                        id = r.id,
                                        foodId = r.id,
                                        foodNames = foodTitle,
                                        programDate = dateStr,
                                        programId = progId,
                                        mealTypeId = slot.mealTypeId,
                                        price = r.price,
                                        selfId = sId,
                                        selfName = sName
                                    )
                                )
                            }

                            programList.add(
                                com.example.data.model.ProgramFoodItem(
                                    programId = progId,
                                    date = dateStr,
                                    dayTranslated = dayName,
                                    foodId = r.id ?: 0L,
                                    foodName = foodTitle,
                                    foodTypeId = 1,
                                    mealTypeId = slot.mealTypeId,
                                    price = if (r.price > 0L) r.price else (if (slot.mealTypeId == 1 || slot.mealTypeId == 6) 100000L else 150000L),
                                    selfId = sId,
                                    selfName = sName,
                                    reserveRuleViolated = r.canReserve == false && !isBooked,
                                    cancelRuleViolated = r.canCancel == false,
                                    daysDifferenceWithToday = r.timeDistanceUntilToday
                                )
                            )
                        }
                    }
                }
            }

            // Group by date
            val groupedPrograms = programList.groupBy { it.date }.values.toList()

            val resultPayload = com.example.data.model.ReserveProgramPayload(
                selfWeekPrograms = groupedPrograms,
                userWeekReserves = userReservesList
            )

            Result.success(resultPayload)
        } catch (e: Exception) {
            Log.e("SamadRepository", "Failed to fetch weekly reserve programs via rest/reserves", e)
            Result.failure(Exception(formatErrorMessage(e)))
        }
    }

    suspend fun executeFoodReserve(
        programId: Long,
        foodTypeId: Int,
        mealTypeId: Int,
        selected: Boolean = true
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val token = ensureValidToken() ?: return@withContext Result.failure(Exception("نشست کاربری منقضی شده است"))
            val api = getApiService()
            val bearer = "Bearer $token"
            val body = com.example.data.model.ReserveRequestBody(
                foodTypeId = foodTypeId,
                mealTypeId = mealTypeId,
                selectedCount = 1,
                freeFoodSelected = false,
                selected = selected
            )

            var success = false
            var lastError: Exception? = null

            // Method 1: PUT /rest/reserves/{programId}/reserve
            try {
                val response = api.executeReserve(bearer, programId, body)
                Log.d("SamadRepository", "Reserve PUT response: ${response.string()}")
                success = true
            } catch (e: Exception) {
                Log.w("SamadRepository", "PUT /rest/reserves/$programId/reserve failed: ${e.message}")
                lastError = e
            }

            // Method 2: POST /rest/reserves/{programId}/reserve
            if (!success) {
                try {
                    val response = api.executeReservePost(bearer, programId, body)
                    Log.d("SamadRepository", "Reserve POST response: ${response.string()}")
                    success = true
                } catch (e: Exception) {
                    Log.w("SamadRepository", "POST /rest/reserves/$programId/reserve failed: ${e.message}")
                    lastError = e
                }
            }

            // Method 3: POST /rest/reserves direct body
            if (!success) {
                try {
                    val directBody = com.example.data.model.ReserveDirectBody(
                        programId = programId,
                        foodTypeId = foodTypeId,
                        mealTypeId = mealTypeId,
                        selectedCount = 1,
                        freeFoodSelected = false,
                        selected = selected
                    )
                    val response = api.executeReserveDirectPost(bearer, directBody)
                    Log.d("SamadRepository", "Reserve Direct POST response: ${response.string()}")
                    success = true
                } catch (e: Exception) {
                    Log.w("SamadRepository", "POST /rest/reserves direct failed: ${e.message}")
                    lastError = e
                }
            }

            if (success) {
                // Refresh user reserves after reservation
                fetchAndCacheReserves()
                Result.success(true)
            } else {
                Result.failure(lastError ?: Exception("خطا در برقراری ارتباط با سامانه رزرو"))
            }
        } catch (e: Exception) {
            Log.e("SamadRepository", "Failed to execute reserve", e)
            Result.failure(e)
        }
    }

    suspend fun clearAllCachedReserves() = withContext(Dispatchers.IO) {
        dao.clearReserves()
        prefs.lastSyncTimestamp = 0L
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.clearAll()
        dao.clearReserves()
        dao.clearMealTypes()
        pendingDao.clearAll()
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }
}
