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

    private fun getApiService(): SamadApiService {
        return ApiClient.createService(prefs.baseUrl) { prefs.accessToken }
    }

    fun getAllCachedReserves(): Flow<List<CachedReserveEntity>> = dao.getAllReserves()

    fun getAllCachedMealTypes(): Flow<List<CachedMealTypeEntity>> = dao.getAllMealTypes()

    suspend fun login(usernameInput: String, passwordInput: String, remember: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = getApiService()
            val tokenResp = api.login(
                username = usernameInput.trim(),
                password = passwordInput.trim()
            )

            val token = tokenResp.accessToken
            prefs.accessToken = token
            // expires_in in seconds -> convert to ms
            val expiryMs = if (tokenResp.expiresIn > 0) {
                System.currentTimeMillis() + (tokenResp.expiresIn * 1000L)
            } else {
                System.currentTimeMillis() + (180L * 24 * 3600 * 1000L)
            }
            prefs.tokenExpiryTimestamp = expiryMs
            prefs.username = usernameInput.trim()
            prefs.rememberPassword = remember
            if (remember) {
                prefs.password = passwordInput.trim()
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

    suspend fun ensureValidToken(): String? = withContext(Dispatchers.IO) {
        val currentToken = prefs.accessToken
        if (prefs.isTokenValid() && !currentToken.isNullOrBlank()) {
            return@withContext currentToken
        }

        // Try re-login if credentials are saved
        val username = prefs.username
        val password = prefs.password
        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            val loginResult = login(username, password, prefs.rememberPassword)
            return@withContext loginResult.getOrNull()
        }

        null
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
                SyncResult.NoReservesFound(checkedWeeks = 4)
            }

        } catch (e: Exception) {
            Log.e("SamadRepository", "Fetch reserves failed", e)
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
            Log.w("SamadRepository", "HTTP error ${e.code()}: $errBody")
            val extracted = ForgotCodeExtractor.extract(errBody)
            val msg = if (!extracted.message.isNullOrBlank()) extracted.message else "خطای سامانه (${e.code()})"
            ExtractedForgotCode(null, message = msg)
        } catch (e: Exception) {
            Log.e("SamadRepository", "getForgotCardCode failed for reserve $reserveId", e)
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
            Log.e("SamadRepository", "fetchFriendReserveForDate failed", e)
            Result.failure(e)
        }
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

    suspend fun clearAllCachedReserves() = withContext(Dispatchers.IO) {
        dao.clearReserves()
        prefs.lastSyncTimestamp = 0L
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.clearAll()
        dao.clearReserves()
        dao.clearMealTypes()
        SamadFoodWidgetProvider.updateAllWidgets(context)
    }
}
