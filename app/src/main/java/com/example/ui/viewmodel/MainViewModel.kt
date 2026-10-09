package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.CachedMealTypeEntity
import com.example.data.local.entity.CachedReserveEntity
import com.example.data.repository.SamadRepository
import com.example.data.repository.SyncResult
import com.example.fcm.FcmHelper
import com.example.util.MealTimeHelper
import com.example.util.PersianDateUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.BuildConfig
import com.example.data.model.GitHubAsset
import com.example.data.model.GitHubRelease
import com.example.util.UpdateCheckResult
import com.example.util.UpdateManager
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val studentName: String? = null,
    val studentNumber: String? = null,
    val studentMajor: String? = null,
    val baseUrl: String = "",
    val allReserves: List<CachedReserveEntity> = emptyList(),
    val mealTypes: List<CachedMealTypeEntity> = emptyList(),
    val availableDates: List<String> = emptyList(),
    val selectedDate: String = "",
    val selectedMealTypeId: Int = 0,
    val dayReservesForMeal: List<CachedReserveEntity> = emptyList(),
    val activeReserve: CachedReserveEntity? = null,
    val mealStatus: MealTimeHelper.MealStatusInfo = MealTimeHelper.MealStatusInfo(
        statusText = "نامشخص",
        isActive = false,
        isExpired = false,
        isUpcoming = false
    ),
    val lastSyncFormatted: String? = null,
    val rememberPassword: Boolean = false,
    val autoSyncDaily: Boolean = true,
    val autoSyncOnLaunch: Boolean = true,
    val remindersEnabled: Boolean = true,
    val cafeteriaBreakfastEnabled: Boolean = false,
    val cafeteriaLunchEnabled: Boolean = true,
    val cafeteriaDinnerEnabled: Boolean = true,
    val cafeteriaSuhurEnabled: Boolean = false,
    val cafeteriaIftarEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val noReservationNotice: String = "",
    val archivedReserves: List<CachedReserveEntity> = emptyList(),
    val isImportingFriendReserve: Boolean = false,
    val fcmEnabled: Boolean = true,
    val fcmToken: String? = null,
    // GitHub Auto-Update state
    val isCheckingUpdate: Boolean = false,
    val updateCheckResult: UpdateCheckResult? = null,
    val isDownloadingUpdate: Boolean = false,
    val downloadProgress: Float = 0f,
    val githubRepo: String = "mr-alirezaw/samad-food-qr",
    val autoCheckUpdates: Boolean = true,
    // Student Preferred Cafeteria state
    val preferredSelfName: String? = null,
    val notifyDifferentSelf: Boolean = true,
    val knownSelfNames: Set<String> = emptySet(),
    val differentSelfWarning: String? = null
) {
    val hasReservationForSelectedMeal: Boolean
        get() = dayReservesForMeal.isNotEmpty()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = SamadRepository(application)
    val prefs = repository.prefs

    private var hasAutoSelectedInitialReserve = false

    private val _uiState = MutableStateFlow(
        MainUiState(
            isLoggedIn = prefs.isTokenValid() || !prefs.username.isNullOrBlank(),
            studentName = prefs.studentName,
            studentNumber = prefs.studentNumber,
            studentMajor = prefs.studentMajor,
            baseUrl = prefs.baseUrl,
            rememberPassword = prefs.rememberPassword,
            autoSyncDaily = prefs.autoSyncDaily,
            autoSyncOnLaunch = prefs.autoSyncOnLaunch,
            remindersEnabled = prefs.mealReminderNotificationEnabled,
            cafeteriaBreakfastEnabled = prefs.cafeteriaBreakfastEnabled,
            cafeteriaLunchEnabled = prefs.cafeteriaLunchEnabled,
            cafeteriaDinnerEnabled = prefs.cafeteriaDinnerEnabled,
            cafeteriaSuhurEnabled = prefs.cafeteriaSuhurEnabled,
            cafeteriaIftarEnabled = prefs.cafeteriaIftarEnabled,
            isDarkMode = prefs.isDarkMode,
            fcmEnabled = prefs.fcmEnabled,
            fcmToken = prefs.fcmToken,
            githubRepo = prefs.githubRepo,
            autoCheckUpdates = prefs.autoCheckUpdates,
            preferredSelfName = prefs.preferredSelfName,
            notifyDifferentSelf = prefs.notifyDifferentSelf,
            knownSelfNames = prefs.knownSelfNames,
            selectedDate = LocalDate.now().toString(),
            selectedMealTypeId = MealTimeHelper.getActiveMealCategory(prefs).defaultMealTypeId
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Initialize FCM & retrieve device registration token
        FcmHelper.initAndFetchToken(application) { token ->
            _uiState.update { it.copy(fcmToken = token) }
        }

        // Automatic GitHub update check on launch
        if (prefs.autoCheckUpdates) {
            checkForUpdates(manual = false)
        }
        // Observe cached database reserves
        viewModelScope.launch {
            repository.getAllCachedReserves().collect { rawCached ->
                val cached = rawCached.filter { it.foodName.isNotBlank() }
                val dates = computeAvailableDates(cached)
                val archived = cached.filter { it.consumed }

                if (!hasAutoSelectedInitialReserve && cached.isNotEmpty()) {
                    autoSelectFirstRelevantReserve(cached)
                    hasAutoSelectedInitialReserve = true
                } else {
                    val currentDate = _uiState.value.selectedDate.ifBlank {
                        val todayStr = LocalDate.now().toString()
                        if (dates.contains(todayStr)) todayStr else (dates.firstOrNull() ?: todayStr)
                    }
                    _uiState.update { current ->
                        current.copy(
                            allReserves = cached,
                            archivedReserves = archived,
                            availableDates = dates,
                            selectedDate = currentDate,
                            lastSyncFormatted = formatLastSync(prefs.lastSyncTimestamp)
                        )
                    }
                    updateSelectedReserve()
                }
            }
        }

        // Observe cached meal types
        viewModelScope.launch {
            repository.getAllCachedMealTypes().collect { types ->
                _uiState.update { current ->
                    current.copy(mealTypes = types)
                }
                val currentTypeId = _uiState.value.selectedMealTypeId
                val activeCat = MealTimeHelper.getActiveMealCategory(prefs)
                val matchingType = types.find { it.name.contains(activeCat.faName) }

                if (currentTypeId == 0 || (types.isNotEmpty() && types.none { it.id == currentTypeId })) {
                    val typeId = matchingType?.id ?: types.firstOrNull()?.id ?: activeCat.defaultMealTypeId
                    selectMealType(typeId)
                } else {
                    updateSelectedReserve()
                }
            }
        }

        // If user already logged in and auto-sync on launch is enabled, perform initial sync
        if (prefs.isTokenValid() && prefs.autoSyncOnLaunch) {
            fetchReserves(isManualRefresh = false)
        }
    }

    fun login(usernameInput: String, passwordInput: String, remember: Boolean) {
        if (usernameInput.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "لطفاً شماره دانشجویی و رمز عبور را وارد کنید.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.login(usernameInput, passwordInput, remember)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        isLoading = false,
                        studentName = prefs.studentName,
                        studentNumber = prefs.studentNumber,
                        studentMajor = prefs.studentMajor,
                        rememberPassword = remember
                    )
                }
                fetchReserves(isManualRefresh = false)
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "ورود ناموفق بود. نام کاربری یا رمز عبور اشتباه است یا سامانه در دسترس نیست."
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                MainUiState(
                    isLoggedIn = false,
                    baseUrl = prefs.baseUrl
                )
            }
        }
    }

    fun fetchReserves(isManualRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isManualRefresh) it.copy(isRefreshing = true, errorMessage = null, infoMessage = null)
                else it.copy(isLoading = true, errorMessage = null, infoMessage = null)
            }

            val result = repository.fetchAndCacheReserves()
            when (result) {
                is SyncResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            infoMessage = "${result.reservesCount} رزرو با موفقیت دریافت و ذخیره شد.",
                            lastSyncFormatted = formatLastSync(prefs.lastSyncTimestamp)
                        )
                    }
                    updateSelectedReserve()
                }
                is SyncResult.NoReservesFound -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            infoMessage = "رزروی در ۳ هفته آینده یافت نشد."
                        )
                    }
                }
                is SyncResult.Error -> {
                    // Check if we have local cache to display offline
                    val cachedCount = repository.getAllCachedReserves().first().size
                    val offlineMode = cachedCount > 0
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = offlineMode,
                            errorMessage = if (offlineMode) "عدم اتصال به سرور - استفاده از داده‌های ذخیره شده" else result.message
                        )
                    }
                }
            }
        }
    }

    fun clearCacheAndRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, infoMessage = null) }
            // Safe Sync & Replace: Never delete the cache beforehand!
            // First fetch from server. If server responds, repository replaces the old cache atomically.
            val result = repository.fetchAndCacheReserves()
            when (result) {
                is SyncResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            infoMessage = "اطلاعات جدید با موفقیت دریافت و جایگزین کش قبلی شد (${result.reservesCount} رزرو).",
                            lastSyncFormatted = formatLastSync(prefs.lastSyncTimestamp)
                        )
                    }
                    updateSelectedReserve()
                }
                is SyncResult.NoReservesFound -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = false,
                            infoMessage = "ارتباط با سامانه برقرار شد؛ در ۳ هفته آینده رزروی یافت نشد."
                        )
                    }
                }
                is SyncResult.Error -> {
                    val cachedCount = repository.getAllCachedReserves().first().size
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isOffline = cachedCount > 0,
                            errorMessage = "${result.message} (جهت جلوگیری از مشکل، اطلاعات قبلی شما حفظ شد)"
                        )
                    }
                }
            }
        }
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        updateSelectedReserve()
    }

    fun selectMealType(mealTypeId: Int) {
        _uiState.update { it.copy(selectedMealTypeId = mealTypeId) }
        updateSelectedReserve()
    }

    fun refreshCurrentMealCode() {
        val currentReserve = _uiState.value.activeReserve ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null, infoMessage = null) }
            val extracted = repository.fetchForgotCodeSingle(currentReserve.reserveId)
            val code = extracted.code
            if (!code.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        infoMessage = "کد غذا دریافت شد: $code"
                    )
                }
            } else {
                val msg = extracted.message ?: "کد غذا از سامانه دریافت نشد. می‌توانید کد را به صورت دستی ثبت کنید."
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = msg
                    )
                }
            }
            updateSelectedReserve()
        }
    }

    fun setManualCode(reserveId: Long, code: String) {
        viewModelScope.launch {
            repository.setManualReserveCode(reserveId, code)
            _uiState.update { it.copy(infoMessage = "کد غذا با موفقیت ذخیره شد: $code") }
            updateSelectedReserve()
        }
    }

    fun updateBaseUrl(newUrl: String) {
        val cleanUrl = newUrl.trim()
        if (cleanUrl.isNotBlank()) {
            prefs.baseUrl = cleanUrl
            _uiState.update { it.copy(baseUrl = prefs.baseUrl, infoMessage = "آدرس سرور ذخیره شد.") }
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        prefs.autoSyncDaily = enabled
        _uiState.update { it.copy(autoSyncDaily = enabled) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.isDarkMode = enabled
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleReminders(enabled: Boolean) {
        prefs.mealReminderNotificationEnabled = enabled
        _uiState.update { it.copy(remindersEnabled = enabled) }
        FcmHelper.syncTopicSubscriptions(getApplication(), prefs)
        if (!enabled) {
            com.example.work.WorkScheduler.cancelAllReminders(getApplication())
        }
    }

    fun toggleFcm(enabled: Boolean) {
        prefs.fcmEnabled = enabled
        _uiState.update { it.copy(fcmEnabled = enabled) }
        FcmHelper.syncTopicSubscriptions(getApplication(), prefs)
        _uiState.update {
            it.copy(infoMessage = if (enabled) "اعلان‌های ابری FCM فعال شدند" else "اعلان‌های ابری FCM غیرفعال شدند")
        }
    }

    fun refreshFcmToken() {
        FcmHelper.initAndFetchToken(getApplication()) { token ->
            _uiState.update {
                it.copy(
                    fcmToken = token,
                    infoMessage = if (!token.isNullOrBlank()) "توکن FCM با موفقیت دریافت و به‌روزرسانی شد" else "دریافت توکن انجام نشد"
                )
            }
        }
    }

    fun sendTestMealNotification() {
        FcmHelper.sendTestMealNotification(getApplication())
        _uiState.update { it.copy(infoMessage = "اعلان تستی یادآور وعده غذا ارسال شد 🔔") }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        prefs.isDarkMode = newMode
        _uiState.update { it.copy(isDarkMode = newMode) }
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.isDarkMode = enabled
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun setCafeteriaMealEnabled(mealType: String, enabled: Boolean) {
        when (mealType) {
            "breakfast" -> {
                prefs.cafeteriaBreakfastEnabled = enabled
                _uiState.update { it.copy(cafeteriaBreakfastEnabled = enabled) }
            }
            "lunch" -> {
                prefs.cafeteriaLunchEnabled = enabled
                _uiState.update { it.copy(cafeteriaLunchEnabled = enabled) }
            }
            "dinner" -> {
                prefs.cafeteriaDinnerEnabled = enabled
                _uiState.update { it.copy(cafeteriaDinnerEnabled = enabled) }
            }
            "suhur" -> {
                prefs.cafeteriaSuhurEnabled = enabled
                _uiState.update { it.copy(cafeteriaSuhurEnabled = enabled) }
            }
            "iftar" -> {
                prefs.cafeteriaIftarEnabled = enabled
                _uiState.update { it.copy(cafeteriaIftarEnabled = enabled) }
            }
        }
        FcmHelper.syncTopicSubscriptions(getApplication(), prefs)
    }

    fun setAutoSyncOnLaunch(enabled: Boolean) {
        prefs.autoSyncOnLaunch = enabled
        _uiState.update { it.copy(autoSyncOnLaunch = enabled) }
    }

    fun importFriendReserve(
        date: String,
        mealTypeId: Int,
        mealName: String,
        foodName: String,
        besideFoods: String,
        selfName: String,
        forgotCode: String,
        friendName: String
    ) {
        viewModelScope.launch {
            val localDate = try { LocalDate.parse(date) } catch (e: Exception) { LocalDate.now() }
            val dayName = PersianDateUtil.getDayOfWeekPersian(localDate)
            val pDate = PersianDateUtil.gregorianToPersian(localDate)
            val dateJStr = "${pDate.year}/${pDate.month}/${pDate.day}"
            val newId = -System.currentTimeMillis()

            val guestEntity = CachedReserveEntity(
                reserveId = newId,
                programId = 0L,
                date = date,
                dateJStr = dateJStr,
                dayName = dayName,
                mealTypeId = mealTypeId,
                mealName = mealName,
                foodName = foodName.ifBlank { "ژتون مهمان" },
                besideFoodNames = besideFoods,
                selfName = selfName.ifBlank { "سلف دانشگاه" },
                forgotCardCode = forgotCode.trim(),
                codeValid = true,
                isGuest = true,
                guestOwnerName = friendName.ifBlank { "دوست دانشجو" },
                syncTimestamp = System.currentTimeMillis()
            )
            repository.insertSingleReserve(guestEntity)
            _uiState.update { it.copy(infoMessage = "ژتون ${guestEntity.foodName} با موفقیت افزوده شد") }
        }
    }

    fun deleteReserve(reserveId: Long) {
        viewModelScope.launch {
            repository.deleteReserve(reserveId)
            _uiState.update { it.copy(infoMessage = "ژتون حذف شد") }
        }
    }

    fun markReserveConsumed(reserveId: Long, consumed: Boolean = true) {
        viewModelScope.launch {
            repository.setReserveConsumed(reserveId, consumed)
            _uiState.update {
                it.copy(infoMessage = if (consumed) "ژتون به بایگانی مصرف‌شده‌ها منتقل شد" else "ژتون به حالت فعال بازگردانده شد")
            }
        }
    }

    fun importFriendReserveFromSamad(
        friendUsername: String,
        friendPassword: String,
        targetDate: String,
        targetMealTypeId: Int? = null,
        onSuccess: (String) -> Unit = {}
    ) {
        if (friendUsername.isBlank() || friendPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "شماره دانشجویی و رمز عبور الزامی است") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImportingFriendReserve = true, errorMessage = null, infoMessage = null) }
            val result = repository.fetchFriendReserveForDate(
                friendUsername = friendUsername.trim(),
                friendPassword = friendPassword.trim(),
                targetDate = targetDate,
                targetMealTypeId = targetMealTypeId
            )

            result.fold(
                onSuccess = { entity ->
                    _uiState.update {
                        it.copy(
                            isImportingFriendReserve = false,
                            infoMessage = "ژتون «${entity.foodName}» متعلق به ${entity.guestOwnerName ?: "دوست دانشجو"} با موفقیت دریافت و اضافه شد 🎉",
                            selectedDate = entity.date,
                            selectedMealTypeId = entity.mealTypeId
                        )
                    }
                    updateSelectedReserve()
                    onSuccess("ژتون با موفقیت اضافه شد")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isImportingFriendReserve = false,
                            errorMessage = error.message ?: "خطا در دریافت ژتون از سامانه سماد"
                        )
                    }
                }
            )
        }
    }

    private fun computeAvailableDates(cached: List<CachedReserveEntity>): List<String> {
        val today = LocalDate.now()
        val todayStr = today.toString()
        val currentSaturday = PersianDateUtil.getWeekStartSaturday(today)
        val thursdayStr = currentSaturday.plusDays(5).toString()
        val fridayStr = currentSaturday.plusDays(6).toString()

        // All 7 days of the current Persian week (Saturday through Friday)
        val currentWeekDates = (0..6).map { currentSaturday.plusDays(it.toLong()).toString() }

        // Future reserve dates beyond this week
        val nextWeekDates = cached.filter { it.date > fridayStr }.map { it.date }

        val allCandidateDates = (currentWeekDates + nextWeekDates).distinct().sorted()

        // Filter: Days with all reserves consumed are archived and omitted from main screen,
        // while Thursday and Friday of current week are always kept as requested.
        return allCandidateDates.filter { dateStr ->
            // Always show Thursday and Friday in the day bar
            if (dateStr == thursdayStr || dateStr == fridayStr) {
                return@filter true
            }
            val reservesOnDate = cached.filter { it.date == dateStr }
            if (dateStr < todayStr && reservesOnDate.isNotEmpty() && reservesOnDate.all { it.consumed }) {
                // Past days where all barcodes are consumed are archived
                false
            } else if (reservesOnDate.isNotEmpty() && reservesOnDate.all { it.consumed } && cached.any { it.date >= todayStr && !it.consumed }) {
                // If today or this day's reserves are all consumed, archive it and move to next active days
                false
            } else {
                true
            }
        }
    }

    private fun autoSelectFirstRelevantReserve(cached: List<CachedReserveEntity>) {
        if (cached.isEmpty()) return
        val dates = computeAvailableDates(cached)
        val todayStr = LocalDate.now().toString()

        val todayReserves = cached.filter { it.date == todayStr }
        val targetReserve = MealTimeHelper.findBestReserveForTime(
            todayReserves = todayReserves,
            allFutureReserves = cached.filter { it.date >= todayStr },
            prefs = prefs
        )

        val activeCat = MealTimeHelper.getActiveMealCategory(prefs)
        val matchingType = _uiState.value.mealTypes.find { it.name.contains(activeCat.faName) }

        val chosenDate = targetReserve?.date ?: (if (dates.contains(todayStr)) todayStr else (dates.firstOrNull() ?: todayStr))
        val chosenMealTypeId = targetReserve?.mealTypeId ?: matchingType?.id ?: activeCat.defaultMealTypeId

        _uiState.update { current ->
            current.copy(
                allReserves = cached,
                archivedReserves = cached.filter { it.consumed },
                availableDates = dates,
                selectedDate = chosenDate,
                selectedMealTypeId = chosenMealTypeId,
                lastSyncFormatted = formatLastSync(prefs.lastSyncTimestamp)
            )
        }
        updateSelectedReserve()
    }

    fun selectActiveReserve(reserve: CachedReserveEntity) {
        if (_uiState.value.dayReservesForMeal.any { it.reserveId == reserve.reserveId }) {
            _uiState.update { it.copy(activeReserve = reserve) }
        }
    }

    private fun updateSelectedReserve() {
        val state = _uiState.value
        val currentDate = state.selectedDate.ifBlank { LocalDate.now().toString() }
        val currentMealTypeId = state.selectedMealTypeId

        // Explicitly filter for current day and currentMealTypeId
        val filteredReserves = if (currentMealTypeId != 0) {
            state.allReserves.filter { it.date == currentDate && it.mealTypeId == currentMealTypeId }
        } else {
            emptyList()
        }

        // Active reserve: prioritize unconsumed reserve so student gets ready barcode
        val match = filteredReserves.find { !it.consumed } ?: filteredReserves.firstOrNull()

        val mealName = match?.mealName
            ?: state.mealTypes.find { it.id == currentMealTypeId }?.name
            ?: MealTimeHelper.MealCategory.entries.find { it.defaultMealTypeId == currentMealTypeId }?.faName
            ?: "غذا"

        val isToday = currentDate == LocalDate.now().toString()
        val noResNotice = if (match == null) {
            if (isToday) "برای امروز $mealName رزرو نکردی" else "برای این تاریخ $mealName رزرو نکردی"
        } else ""

        val status = MealTimeHelper.calculateMealStatus(
            dateStr = currentDate,
            mealName = mealName,
            prefs = prefs
        )

        // Check if reserved cafeteria differs from user's preferred cafeteria
        val preferredSelf = prefs.preferredSelfName
        val differentSelfNotice = if (!preferredSelf.isNullOrBlank() && prefs.notifyDifferentSelf && match != null && match.selfName.isNotBlank() && !match.selfName.contains(preferredSelf, ignoreCase = true)) {
            "توجه: غذای این وعده در «${match.selfName}» رزرو شده است (سلف انتخابی شما: «$preferredSelf»)"
        } else null

        _uiState.update {
            it.copy(
                selectedDate = currentDate,
                dayReservesForMeal = filteredReserves,
                activeReserve = match,
                mealStatus = status,
                noReservationNotice = noResNotice,
                differentSelfWarning = differentSelfNotice,
                preferredSelfName = prefs.preferredSelfName,
                knownSelfNames = prefs.knownSelfNames
            )
        }
    }

    fun setPreferredSelf(selfName: String?) {
        val clean = selfName?.trim()?.takeIf { it.isNotBlank() }
        prefs.preferredSelfName = clean
        _uiState.update {
            it.copy(
                preferredSelfName = clean,
                infoMessage = if (clean != null) "سلف «$clean» به عنوان سلف پیش‌فرض انتخاب شد" else "فیلتر سلف غیرفعال شد"
            )
        }
        updateSelectedReserve()
    }

    fun setNotifyDifferentSelf(enabled: Boolean) {
        prefs.notifyDifferentSelf = enabled
        _uiState.update { it.copy(notifyDifferentSelf = enabled) }
        updateSelectedReserve()
    }

    fun setGithubRepo(repo: String) {
        val clean = repo.trim().removePrefix("https://github.com/").removePrefix("github.com/").trim('/')
        prefs.githubRepo = clean
        _uiState.update { it.copy(githubRepo = clean, infoMessage = "مخزن بروزرسانی گیت‌هاب تنظیم شد: $clean") }
    }

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefs.autoCheckUpdates = enabled
        _uiState.update { it.copy(autoCheckUpdates = enabled) }
    }

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true) }
            val currentVersion = BuildConfig.VERSION_NAME
            val repo = prefs.githubRepo
            val result = UpdateManager.checkForUpdate(repo, currentVersion)
            prefs.lastUpdateCheckTimestamp = System.currentTimeMillis()

            _uiState.update { current ->
                current.copy(
                    isCheckingUpdate = false,
                    updateCheckResult = result,
                    infoMessage = when {
                        result is UpdateCheckResult.UpToDate && manual -> "برنامه کاملاً به‌روز است (نسخه $currentVersion)"
                        result is UpdateCheckResult.Error && manual -> result.message
                        else -> current.infoMessage
                    }
                )
            }

            if (result is UpdateCheckResult.UpdateAvailable && !manual) {
                // If found in background launch check, notify student
                UpdateManager.notifyUserOfUpdate(getApplication(), result.release)
            }
        }
    }

    fun dismissUpdateDialog() {
        _uiState.update { it.copy(updateCheckResult = null) }
    }

    fun downloadAndInstallUpdate(context: Context, release: GitHubRelease, asset: GitHubAsset) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingUpdate = true, downloadProgress = 0f) }
            val downloadUrl = asset.downloadUrl
            val fileName = "SamadFoodQR-${release.displayVersion}.apk"

            val downloadResult = UpdateManager.downloadApk(
                context = context,
                downloadUrl = downloadUrl,
                fileName = fileName
            ) { progress ->
                _uiState.update { it.copy(downloadProgress = progress) }
            }

            downloadResult.fold(
                onSuccess = { apkFile ->
                    _uiState.update {
                        it.copy(
                            isDownloadingUpdate = false,
                            downloadProgress = 1f,
                            infoMessage = "دانلود کامل شد. در حال باز کردن نصاب برنامه..."
                        )
                    }
                    val installResult = UpdateManager.installApk(context, apkFile)
                    if (installResult.isFailure) {
                        _uiState.update {
                            it.copy(errorMessage = "خطا در اجرای نصاب: ${installResult.exceptionOrNull()?.message}")
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDownloadingUpdate = false,
                            errorMessage = "دانلود فایل با خطا مواجه شد: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    private fun formatLastSync(timestamp: Long): String? {
        if (timestamp == 0L) return null
        val diffSec = (System.currentTimeMillis() - timestamp) / 1000
        return when {
            diffSec < 60 -> "چند لحظه پیش"
            diffSec < 3600 -> "${diffSec / 60} دقیقه پیش"
            diffSec < 86400 -> "${diffSec / 3600} ساعت پیش"
            else -> "${diffSec / 86400} روز پیش"
        }
    }
}
