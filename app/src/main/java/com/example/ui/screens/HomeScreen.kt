package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.LinearProgressIndicator
import com.example.util.UpdateCheckResult
import com.example.util.UpdateManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.TabRow
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AmbientOrbsScaffold
import com.example.ui.components.SamadBrandTitle
import com.example.ui.theme.SamadBackground
import com.example.ui.theme.SamadDarkBackground
import com.example.ui.theme.SamadDarkPrimary
import com.example.ui.theme.SamadOnSurface
import com.example.ui.theme.SamadOnSurfaceVariant
import com.example.ui.theme.SamadOutline
import com.example.ui.theme.SamadPrimary
import com.example.ui.theme.SamadPrimaryContainer
import com.example.ui.theme.SamadPrimaryFixed
import com.example.ui.theme.SamadSecondary
import com.example.ui.theme.SamadSurfaceBright
import com.example.ui.theme.SamadSurfaceContainer
import com.example.ui.theme.SamadSurfaceContainerLow
import com.example.ui.theme.SamadSurfaceContainerLowest
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusExpired
import com.example.ui.theme.StatusUpcoming
import com.example.util.toPersianDigits
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ExportHelper
import com.example.util.MealTimeHelper
import com.example.util.PersianDateUtil
import com.example.util.QrCodeGenerator
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onNavigateToSettings: () -> Unit,
    onNavigateToAllReserves: () -> Unit,
    onNavigateToMessenger: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddFriendReserveDialog by remember { mutableStateOf(false) }

    // Screen brightness & keep-screen-on when showing QR
    val hasQrCode = !uiState.activeReserve?.forgotCardCode.isNullOrBlank()
    DisposableEffect(hasQrCode) {
        val activity = context as? Activity
        val originalBrightness = activity?.window?.attributes?.screenBrightness
        if (hasQrCode) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val layout = activity?.window?.attributes
            layout?.screenBrightness = 1.0f
            activity?.window?.attributes = layout
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val layout = activity?.window?.attributes
            layout?.screenBrightness = originalBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity?.window?.attributes = layout
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.infoMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // RTL for Persian interface
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmbientOrbsScaffold {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    HomeTopBar(
                        uiState = uiState,
                        onRefresh = { viewModel.fetchReserves(isManualRefresh = true) },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onNavigateToAllReserves = onNavigateToAllReserves,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToMessenger = onNavigateToMessenger
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.fetchReserves(isManualRefresh = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .testTag("pull_to_refresh_box")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Offline banner if applicable
                        AnimatedVisibility(visible = uiState.isOffline) {
                            OfflineBadgeBanner()
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // GitHub Update Available Banner if applicable
                        val updateResult = uiState.updateCheckResult
                        AnimatedVisibility(visible = updateResult is UpdateCheckResult.UpdateAvailable) {
                            if (updateResult is UpdateCheckResult.UpdateAvailable) {
                                HomeUpdateBanner(
                                    update = updateResult,
                                    isDownloading = uiState.isDownloadingUpdate,
                                    downloadProgress = uiState.downloadProgress,
                                    onDownloadClick = { asset ->
                                        if (asset != null) {
                                            viewModel.downloadAndInstallUpdate(context, updateResult.release, asset)
                                        } else {
                                            UpdateManager.openInBrowser(context, updateResult.release.htmlUrl)
                                        }
                                    },
                                    onOpenBrowser = {
                                        UpdateManager.openInBrowser(context, updateResult.release.htmlUrl)
                                    },
                                    onDismiss = { viewModel.dismissUpdateDialog() }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Warning if active reserve is in a different dining hall than student's preferred cafeteria
                        AnimatedVisibility(visible = uiState.differentSelfWarning != null) {
                            uiState.differentSelfWarning?.let { warningText ->
                                DifferentSelfWarningBanner(
                                    warningText = warningText,
                                    onSettingsClick = onNavigateToSettings
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        // Greeting & Quick Status Row
                        GreetingAndStatusRow(
                            studentName = uiState.studentName ?: viewModel.prefs.username ?: "دانشجو",
                            isCafeteriaActive = uiState.mealStatus.isActive,
                            hasActiveReserveForMeal = uiState.dayReservesForMeal.isNotEmpty()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Days selector row
                        if (uiState.availableDates.isNotEmpty()) {
                            DaySelectorRow(
                                dates = uiState.availableDates,
                                selectedDate = uiState.selectedDate,
                                onDateSelected = { viewModel.selectDate(it) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Dynamic Meal Tabs with reservation indicators
                        MealTabsSection(
                            mealTypes = uiState.mealTypes,
                            selectedMealTypeId = uiState.selectedMealTypeId,
                            selectedDate = uiState.selectedDate,
                            allReserves = uiState.allReserves,
                            cafeteriaBreakfastEnabled = uiState.cafeteriaBreakfastEnabled,
                            cafeteriaLunchEnabled = uiState.cafeteriaLunchEnabled,
                            cafeteriaDinnerEnabled = uiState.cafeteriaDinnerEnabled,
                            cafeteriaSuhurEnabled = uiState.cafeteriaSuhurEnabled,
                            cafeteriaIftarEnabled = uiState.cafeteriaIftarEnabled,
                            viewModel = viewModel,
                            onTabSelected = { viewModel.selectMealType(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // HERO BENTO: Active Jetton Slider / QR Card or No Reservation State
                        if (uiState.dayReservesForMeal.isNotEmpty()) {
                            HeroReservesSliderCard(
                                reserves = uiState.dayReservesForMeal,
                                mealStatus = uiState.mealStatus,
                                isRefreshing = uiState.isRefreshing,
                                studentName = uiState.studentName,
                                studentNumber = uiState.studentNumber ?: viewModel.prefs.username,
                                onCopyCode = { code ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Food Code", code)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "کد $code کپی شد", Toast.LENGTH_SHORT).show()
                                },
                                onRefreshCode = { viewModel.refreshCurrentMealCode() },
                                onSaveManualCode = { reserveId, code ->
                                    viewModel.setManualCode(reserveId, code)
                                },
                                onDeleteGuestReserve = { reserveId ->
                                    viewModel.deleteReserve(reserveId)
                                }
                            )
                        } else {
                            val currentMealName = uiState.mealTypes.find { it.id == uiState.selectedMealTypeId }?.name
                                ?: MealTimeHelper.MealCategory.entries.find { it.defaultMealTypeId == uiState.selectedMealTypeId }?.faName
                                ?: "این وعده"
                            val otherMealsToday = uiState.allReserves.filter { it.date == uiState.selectedDate && it.mealTypeId != uiState.selectedMealTypeId }
                            HeroNoReservationCard(
                                selectedMealName = currentMealName,
                                selectedDate = uiState.selectedDate,
                                noReservationNotice = uiState.noReservationNotice,
                                otherReservesOnDate = otherMealsToday,
                                isLoading = uiState.isLoading,
                                onSelectMeal = { mealTypeId -> viewModel.selectMealType(mealTypeId) },
                                onFetchClick = { viewModel.fetchReserves(isManualRefresh = true) },
                                onAddFriendReserve = { showAddFriendReserveDialog = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Secondary Bento: Weekly utility actions & widget pinning
                        WeekUtilityActionsCard(
                            allReserves = uiState.allReserves,
                            onExportPdf = {
                                ExportHelper.exportWeekReservesToPdf(
                                    context = context,
                                    reserves = uiState.allReserves,
                                    weekTitle = "برنامه غذایی هفته",
                                    studentName = uiState.studentName,
                                    studentNumber = uiState.studentNumber ?: viewModel.prefs.username,
                                    universityName = "دانشگاه فنی و حرفه‌ای"
                                )
                            },
                            onShareGroup = {
                                ExportHelper.shareGroupReserves(
                                    context = context,
                                    reserves = uiState.allReserves,
                                    title = "برنامه غذایی هفته",
                                    studentName = uiState.studentName,
                                    studentNumber = uiState.studentNumber ?: viewModel.prefs.username
                                )
                            },
                            onAddFriendReserve = { showAddFriendReserveDialog = true },
                            onViewAll = onNavigateToAllReserves
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Secondary Bento: Cache / Sync Reserves
                        SyncReservesActionCard(
                            isLoading = uiState.isLoading,
                            isRefreshing = uiState.isRefreshing,
                            lastSync = uiState.lastSyncFormatted,
                            onFetch = { viewModel.fetchReserves(isManualRefresh = true) }
                        )

                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }

            if (showAddFriendReserveDialog) {
                AddFriendReserveDialog(
                    selectedDate = uiState.selectedDate,
                    isImporting = uiState.isImportingFriendReserve,
                    onDismiss = { showAddFriendReserveDialog = false },
                    onFetchFromSamad = { friendUsername, friendPassword, targetDate, targetMealId ->
                        viewModel.importFriendReserveFromSamad(
                            friendUsername = friendUsername,
                            friendPassword = friendPassword,
                            targetDate = targetDate,
                            targetMealTypeId = targetMealId,
                            onSuccess = {
                                showAddFriendReserveDialog = false
                            }
                        )
                    },
                    onSaveManual = { date, mealTypeId, mealName, foodName, besideFoods, selfName, forgotCode, friendName ->
                        viewModel.importFriendReserve(
                            date = date,
                            mealTypeId = mealTypeId,
                            mealName = mealName,
                            foodName = foodName,
                            besideFoods = besideFoods,
                            selfName = selfName,
                            forgotCode = forgotCode,
                            friendName = friendName
                        )
                        showAddFriendReserveDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Top Header matching the Stitch HTML layout:
 * Left (RTL): Profile Avatar button + Notification button
 * Right (RTL): University caption, "سَماد" with superscript green "QR" badge, and Samad logo
 */
@Composable
private fun HomeTopBar(
    uiState: MainUiState,
    onRefresh: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onNavigateToAllReserves: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMessenger: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Action Icons (Profile Avatar, Dark Mode, Refresh, All Reserves)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile squircle button with emerald gradient
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SamadPrimary, Color(0xFF10B981))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(14.dp))
                    .clickable { onNavigateToSettings() }
                    .testTag("settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "پروفایل و تنظیمات",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dark Mode toggle button (Item 1 requirement)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onToggleDarkMode() }
                    .testTag("dark_mode_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "تغییر حالت شب/روز",
                    tint = if (uiState.isDarkMode) SamadSecondary else SamadPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Notification / Refresh squircle button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onRefresh() }
                    .testTag("refresh_action_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "به‌روزرسانی",
                    tint = SamadPrimary,
                    modifier = Modifier
                        .size(19.dp)
                        .rotate(if (uiState.isRefreshing || uiState.isLoading) rotation else 0f)
                )
            }

            // All reserves list button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToAllReserves() }
                    .testTag("all_reserves_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "برنامه هفتگی",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Messenger shortcut button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToMessenger() }
                    .testTag("messenger_top_button"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (uiState.messengerUnreadCount > 0) {
                            Badge {
                                Text(text = "${uiState.messengerUnreadCount}")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "پیام‌رسان ژتون",
                        tint = SamadPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // App Brand Title with Superscript Square Badge and Logo
        SamadBrandTitle(
            universityName = "دانشگاه فنی و حرفه‌ای"
        )
    }
}

/**
 * Greeting row with student name, date, and live cafeteria status pill
 */
@Composable
private fun GreetingAndStatusRow(
    studentName: String,
    isCafeteriaActive: Boolean,
    hasActiveReserveForMeal: Boolean = false
) {
    val today = LocalDate.now()
    val dayName = PersianDateUtil.getDayOfWeekPersian(today)
    val pDate = PersianDateUtil.gregorianToPersian(today)

    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "سلام، $studentName 👋",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "امروز $dayName، ${pDate.day.toPersianDigits()} ${pDate.monthName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Live cafeteria status pill - only shown if student actually has a reserve for this meal!
        if (hasActiveReserveForMeal) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SamadPrimary.copy(alpha = 0.12f))
                    .border(1.dp, SamadPrimary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            (if (isCafeteriaActive) SamadPrimary else SamadSecondary).copy(alpha = alpha)
                        )
                )
                Text(
                    text = if (isCafeteriaActive) "زمان توزیع غذا" else "رزرو فعال",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamadPrimary
                )
            }
        }
    }
}

@Composable
private fun OfflineBadgeBanner() {
    val isDark = MaterialTheme.colorScheme.background == SamadDarkBackground
    Card(
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2A1C00) else Color(0xFFFEF3C7)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF4D3400) else Color(0xFFFDE68A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = if (isDark) Color(0xFFFFB95F) else SamadSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "حالت آفلاین • اطلاعات از حافظه دستگاه نمایش داده می‌شود",
                color = if (isDark) Color(0xFFFFB95F) else SamadSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DaySelectorRow(
    dates: List<String>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val todayStr = LocalDate.now().toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dates.forEach { dateStr ->
            val isSelected = dateStr == selectedDate
            val isToday = dateStr == todayStr
            val localDate = try { LocalDate.parse(dateStr) } catch (e: Exception) { LocalDate.now() }
            val dayPersian = PersianDateUtil.getDayOfWeekPersian(localDate)
            val pDate = PersianDateUtil.gregorianToPersian(localDate)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) SamadPrimary else SamadSurfaceContainerLowest
                    )
                    .border(
                        1.dp,
                        if (isSelected) SamadPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onDateSelected(dateStr) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayPersian,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${pDate.day.toPersianDigits()} ${pDate.monthName}",
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isToday) {
                Text(
                    text = "امروز",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else SamadPrimary
                )
            }
        }
            }
        }
    }
}

@Composable
private fun MealTabsSection(
    mealTypes: List<com.example.data.local.entity.CachedMealTypeEntity>,
    selectedMealTypeId: Int,
    selectedDate: String,
    allReserves: List<com.example.data.local.entity.CachedReserveEntity>,
    cafeteriaBreakfastEnabled: Boolean,
    cafeteriaLunchEnabled: Boolean,
    cafeteriaDinnerEnabled: Boolean,
    cafeteriaSuhurEnabled: Boolean,
    cafeteriaIftarEnabled: Boolean,
    viewModel: MainViewModel,
    onTabSelected: (Int) -> Unit
) {
    val rawList = if (mealTypes.isNotEmpty()) {
        mealTypes
    } else {
        listOf(
            com.example.data.local.entity.CachedMealTypeEntity(6, "صبحانه", 1),
            com.example.data.local.entity.CachedMealTypeEntity(7, "ناهار", 2),
            com.example.data.local.entity.CachedMealTypeEntity(8, "شام", 3)
        )
    }

    val filteredList = rawList.filter { meal ->
        val isMainMeal = meal.id == 7 || meal.id == 8 || meal.name.contains("ناهار") || meal.name.contains("شام")
        val hasActiveReserveOnDate = allReserves.any { it.date == selectedDate && it.mealTypeId == meal.id && !it.consumed }
        val hasAnyActiveReserve = allReserves.any { it.mealTypeId == meal.id && !it.consumed }

        val isEnabledInSettings = when {
            meal.id == 6 || meal.name.contains("صبحانه") -> cafeteriaBreakfastEnabled
            meal.id == 7 || meal.name.contains("ناهار") -> cafeteriaLunchEnabled
            meal.id == 8 || meal.name.contains("شام") -> cafeteriaDinnerEnabled
            meal.name.contains("افطار") -> cafeteriaIftarEnabled
            meal.name.contains("سحر") -> cafeteriaSuhurEnabled
            else -> false
        }

        if (isMainMeal) {
            // Main meals: enabled in Settings by default, or shown if student has active reserve
            isEnabledInSettings || hasAnyActiveReserve
        } else {
            // Secondary meals (breakfast, iftar, suhur, etc.):
            // By default NOT shown, unless enabled in Settings OR user has active reservation!
            isEnabledInSettings || hasActiveReserveOnDate || hasAnyActiveReserve
        }
    }.sortedBy { it.disPriority }

    val listToDisplay = if (filteredList.isNotEmpty()) {
        filteredList
    } else {
        rawList.filter { it.id == 7 || it.id == 8 || it.name.contains("ناهار") || it.name.contains("شام") }
            .ifEmpty { rawList }
            .sortedBy { it.disPriority }
    }
    val selectedIndex = listToDisplay.indexOfFirst { it.id == selectedMealTypeId }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = SamadSurfaceContainerLowest,
        contentColor = SamadPrimary,
        edgePadding = 6.dp,
        indicator = {},
        divider = {},
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
    ) {
        listToDisplay.forEach { meal ->
            val isSelected = meal.id == selectedMealTypeId
            val hasReserve = allReserves.any { it.date == selectedDate && it.mealTypeId == meal.id }

            Tab(
                selected = isSelected,
                onClick = { onTabSelected(meal.id) },
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) SamadPrimary else Color.Transparent)
                    .testTag("meal_tab_${meal.id}"),
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = meal.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else SamadOnSurfaceVariant
                        )

                        if (hasReserve) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White.copy(alpha = 0.25f) else SamadPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "رزرو",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SamadPrimary
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * HERO BENTO: Slide / Pager for single or multiple food reservations in the same mealtime.
 * Displays count of reserves for that meal slot only, and allows horizontal slide between them.
 */
@Composable
private fun HeroReservesSliderCard(
    reserves: List<com.example.data.local.entity.CachedReserveEntity>,
    mealStatus: MealTimeHelper.MealStatusInfo,
    isRefreshing: Boolean,
    studentName: String?,
    studentNumber: String?,
    onCopyCode: (String) -> Unit,
    onRefreshCode: () -> Unit,
    onSaveManualCode: (Long, String) -> Unit,
    onDeleteGuestReserve: (Long) -> Unit
) {
    if (reserves.isEmpty()) return

    val mealName = reserves.firstOrNull()?.mealName ?: "این وعده"
    val pagerState = rememberPagerState(pageCount = { reserves.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Multi-reservation indicator banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (reserves.size > 1) SamadPrimary.copy(alpha = 0.12f) else SamadSurfaceContainerLowest
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (reserves.size > 1) SamadPrimary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LunchDining,
                        contentDescription = null,
                        tint = SamadPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (reserves.size > 1)
                            "تعداد رزرو برای این وعده ($mealName): ${reserves.size.toPersianDigits()} مورد (اسلاید کنید ↔)"
                        else
                            "رزرو ثبت‌شده برای این وعده ($mealName)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamadOnSurface
                    )
                }

                if (reserves.size > 1) {
                    Text(
                        text = "رزرو ${(pagerState.currentPage + 1).toPersianDigits()} از ${reserves.size.toPersianDigits()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SamadPrimary
                    )
                }
            }
        }

        if (reserves.size == 1) {
            val single = reserves.first()
            HeroActiveReserveCard(
                reserve = single,
                mealStatus = mealStatus,
                isRefreshing = isRefreshing,
                studentName = studentName,
                studentNumber = studentNumber,
                onCopyCode = onCopyCode,
                onRefreshCode = onRefreshCode,
                onSaveManualCode = onSaveManualCode,
                onDeleteGuestReserve = if (single.isGuest) { { onDeleteGuestReserve(single.reserveId) } } else null
            )
        } else {
            // Horizontal Pager for multi-reserve sliding
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val item = reserves[page]
                HeroActiveReserveCard(
                    reserve = item,
                    mealStatus = mealStatus,
                    isRefreshing = isRefreshing,
                    studentName = studentName,
                    studentNumber = studentNumber,
                    onCopyCode = onCopyCode,
                    onRefreshCode = onRefreshCode,
                    onSaveManualCode = onSaveManualCode,
                    onDeleteGuestReserve = if (item.isGuest) { { onDeleteGuestReserve(item.reserveId) } } else null
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation dots & buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    },
                    enabled = pagerState.currentPage > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "قبلی", modifier = Modifier.size(16.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    repeat(reserves.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) SamadPrimary else SamadOutline.copy(alpha = 0.3f))
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (pagerState.currentPage < reserves.size - 1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    enabled = pagerState.currentPage < reserves.size - 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "بعدی", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * HERO BENTO: Active Jetton & QR Card
 * Replicates the exact Stitch HTML layout:
 * - Meal badge & status banner ("ژتون ناهار امروز", "چلو جوجه کباب", "آماده تحویل")
 * - 2 specs pills: Location + Time
 * - Accompaniments / Side dishes (مخلفات)
 * - QR visual block with soft border
 * - 9-digit forgot card code (chunk1 — chunk2 — chunk3)
 * - Action buttons: share & copy
 */
@Composable
private fun HeroActiveReserveCard(
    reserve: com.example.data.local.entity.CachedReserveEntity,
    mealStatus: MealTimeHelper.MealStatusInfo,
    isRefreshing: Boolean,
    studentName: String?,
    studentNumber: String?,
    onCopyCode: (String) -> Unit,
    onRefreshCode: () -> Unit,
    onSaveManualCode: (Long, String) -> Unit,
    onDeleteGuestReserve: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember { com.example.data.security.SecurePrefs(context) }
    var showManualCodeDialog by remember { mutableStateOf(false) }
    var manualCodeInput by remember(reserve.reserveId, reserve.forgotCardCode) {
        mutableStateOf(reserve.forgotCardCode ?: "")
    }
    var showStudentBarcode by remember { mutableStateOf(false) }

    val forgotCode = reserve.forgotCardCode ?: ""
    val hasForgotCode = forgotCode.isNotBlank()

    val qrContent = when {
        showStudentBarcode && !studentNumber.isNullOrBlank() -> studentNumber
        hasForgotCode -> forgotCode
        else -> ""
    }

    val qrBitmap = remember(qrContent) {
        if (qrContent.isNotBlank()) QrCodeGenerator.generateQrBitmap(qrContent, 800) else null
    }

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_reserve_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Meal Badge & Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LunchDining,
                            contentDescription = null,
                            tint = SamadPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (reserve.isGuest) "ژتون مهمان (${reserve.guestOwnerName ?: "دانشجو"})" else "ژتون ${reserve.mealName} امروز",
                            fontSize = 11.sp,
                            color = if (reserve.isGuest) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = reserve.foodName.ifBlank { reserve.mealName },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reserve.isGuest && onDeleteGuestReserve != null) {
                        IconButton(
                            onClick = onDeleteGuestReserve,
                            modifier = Modifier.size(28.dp).padding(end = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف ژتون مهمان",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (reserve.consumed) {
                        // Consumed / Archived status pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF64748B))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "مصرف‌شده (بایگانی)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Ready status pill with check_circle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (reserve.isGuest) Color(0xFFD97706) else SamadPrimary)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (reserve.isGuest) "مهمان" else "آماده تحویل",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2 Spec Pills: Location & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Location pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = SamadPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = reserve.selfName.ifBlank { "سلف دانشگاه" },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Time pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SamadPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    val servingWindow = MealTimeHelper.getServingWindow(MealTimeHelper.MealCategory.fromName(reserve.mealName), prefs)
                    Text(
                        text = "توزیع: ${servingWindow.formatRange()}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Accompaniments / Side dishes (مخلفات غذا)
            val beside = reserve.besideFoodNames.trim()
            if (beside.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, SamadPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = SamadPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مخلفات: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamadPrimary
                    )
                    Text(
                        text = beside,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reserve.consumed) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SamadPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "این ژتون در سلف مصرف شده است",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "در سامانه سماد تحویل داده شده ثبت گردیده و به بخش بایگانی منتقل شد.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Large Center QR Visual Block (Barcode background is strictly pure white with solid white border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 340.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(4.dp, Color.White, RoundedCornerShape(22.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code غذا",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        if (showStudentBarcode) {
                            Text(
                                text = "بارکد شماره دانشجویی",
                                fontSize = 10.sp,
                                color = SamadPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = SamadPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "در حال دریافت کد از سامانه...",
                                color = Color(0xFF475569),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = SamadSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "کد فراموشی دریافت نشد",
                                color = Color(0xFF0F172A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "کد در ساعات نزدیک سرو صادر می‌شود",
                                color = Color(0xFF475569),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onRefreshCode,
                                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("دریافت مجدد", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = { showManualCodeDialog = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("ورود دستی", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (showStudentBarcode) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(onClick = { showStudentBarcode = false }) {
                    Text("بازگشت به کد سلف", fontSize = 11.sp, color = SamadPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 9-Digit Forgot Card Code Display
            if (hasForgotCode) {
                val cleanDigits = forgotCode.filter { it.isDigit() }
                val chunk1 = if (cleanDigits.length >= 3) cleanDigits.substring(0, 3) else cleanDigits
                val chunk2 = if (cleanDigits.length >= 6) cleanDigits.substring(3, 6) else (if (cleanDigits.length > 3) cleanDigits.substring(3) else "")
                val chunk3 = if (cleanDigits.length >= 9) cleanDigits.substring(6, 9) else (if (cleanDigits.length > 6) cleanDigits.substring(6) else "")

                Card(
                    colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopyCode(forgotCode) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "کد فراموشی کارت (۹ رقمی)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SamadOnSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    manualCodeInput = forgotCode
                                    showManualCodeDialog = true
                                }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ویرایش کد",
                                        tint = SamadOutline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(onClick = { onCopyCode(forgotCode) }, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "کپی کد",
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Render 3 distinct chunks strictly LTR
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BentoDigitChunkBox(chunk1)
                                Text("—", color = SamadOutline, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                BentoDigitChunkBox(chunk2)
                                Text("—", color = SamadOutline, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                BentoDigitChunkBox(chunk3)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Share Single Reserve + Copy Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        ExportHelper.shareSingleReserve(
                            context = context,
                            reserve = reserve,
                            qrBitmap = qrBitmap,
                            studentName = studentName,
                            studentNumber = studentNumber
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("share_single_reserve_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اشتراک ژتون", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onCopyCode(forgotCode) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("copy_code_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("کپی کد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Manual Code Input Dialog
    if (showManualCodeDialog) {
        AlertDialog(
            onDismissRequest = { showManualCodeDialog = false },
            title = {
                Text(
                    text = "ثبت دستی کد فراموشی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "در صورتی که کد ۹ رقمی را دارید، اینجا وارد کنید تا فوراً بارکد بزرگ نمایش داده شود:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = manualCodeInput,
                        onValueChange = { manualCodeInput = it.filter { ch -> ch.isDigit() }.take(14) },
                        label = { Text("کد فراموشی کارت") },
                        placeholder = { Text("مثال: 987654321") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualCodeInput.isNotBlank()) {
                            onSaveManualCode(reserve.reserveId, manualCodeInput)
                            showManualCodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ذخیره و تولید QR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualCodeDialog = false }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun BentoDigitChunkBox(chunk: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, SamadPrimary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = chunk.toPersianDigits(),
            fontFamily = FontFamily.Monospace,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = SamadPrimary
        )
    }
}

@Composable
private fun HeroNoReservationCard(
    selectedMealName: String,
    selectedDate: String,
    noReservationNotice: String = "",
    otherReservesOnDate: List<com.example.data.local.entity.CachedReserveEntity>,
    isLoading: Boolean,
    onSelectMeal: (Int) -> Unit,
    onFetchClick: () -> Unit,
    onAddFriendReserve: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("no_reservation_state")
            .testTag("no_reservation_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SamadSurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = "No Reservation",
                    tint = SamadPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SamadSecondary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("no_reservation_badge")
            ) {
                Text(
                    text = "بدون رزرو برای این وعده",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamadSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayTitle = if (noReservationNotice.isNotBlank()) {
                noReservationNotice
            } else {
                "برای این وعده $selectedMealName رزرو نکردی"
            }

            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = SamadOnSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("no_reservation_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "در تاریخ ($selectedDate)، برای وعده $selectedMealName رزروی در سامانه ثبت نشده است.",
                fontSize = 12.sp,
                color = SamadOnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("no_reservation_description")
            )

            if (otherReservesOnDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "سایر رزروهای ثبت‌شده در این روز:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamadPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    otherReservesOnDate.forEach { other ->
                        FilterChip(
                            selected = false,
                            onClick = { onSelectMeal(other.mealTypeId) },
                            label = {
                                Text(
                                    text = "${other.mealName}: ${other.foodName.ifBlank { "غذا" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = SamadSurfaceContainerLow,
                                labelColor = SamadPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onFetchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).testTag("no_reservation_refresh_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("بروزرسانی دستی")
                    }
                }

                OutlinedButton(
                    onClick = onAddFriendReserve,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadPrimary)
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ژتون دانشجوی دیگر")
                }
            }
        }
    }
}

@Composable
private fun WeekUtilityActionsCard(
    allReserves: List<com.example.data.local.entity.CachedReserveEntity>,
    onExportPdf: () -> Unit,
    onShareGroup: () -> Unit,
    onAddFriendReserve: () -> Unit,
    onViewAll: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("week_utility_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ابزارهای برنامه غذایی هفتگی",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamadOnSurface
                )
                Text(
                    text = "${allReserves.size} رزرو ثبت‌شده",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SamadPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_export_pdf_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("خروجی PDF", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onShareGroup,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_share_group_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadSecondary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اشتراک هفته", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddFriendReserve,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadPrimary)
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("افزودن ژتون دوست", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        com.example.widget.SamadFoodWidgetProvider.requestPinWidget(context)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("card_pin_widget_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SamadSurfaceContainerLow,
                        contentColor = SamadOnSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = SamadPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ویجت گوشی", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddFriendReserveDialog(
    selectedDate: String,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onFetchFromSamad: (friendUsername: String, friendPassword: String, targetDate: String, targetMealId: Int?) -> Unit,
    onSaveManual: (
        date: String,
        mealTypeId: Int,
        mealName: String,
        foodName: String,
        besideFoods: String,
        selfName: String,
        forgotCode: String,
        friendName: String
    ) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Auto fetch with Samad credentials, 1 = Manual

    // Auto fetch state
    var friendUsername by remember { mutableStateOf("") }
    var friendPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var chosenDate by remember { mutableStateOf(selectedDate.ifBlank { LocalDate.now().toString() }) }
    var chosenMealId by remember { mutableStateOf<Int?>(null) } // null = all

    // Manual entry state
    var friendName by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var besideFoods by remember { mutableStateOf("") }
    var manualMealId by remember { mutableStateOf(7) } // 7 = Lunch
    var manualMealName by remember { mutableStateOf("ناهار") }
    var forgotCode by remember { mutableStateOf("") }
    var selfName by remember { mutableStateOf("سلف دانشگاه") }

    val today = LocalDate.now()
    val saturday = PersianDateUtil.getWeekStartSaturday(today)
    val selectableDays = remember(saturday) {
        (0 until 14).map { offset ->
            val d = saturday.plusDays(offset.toLong())
            val p = PersianDateUtil.gregorianToPersian(d)
            val dayName = PersianDateUtil.getDayOfWeekPersian(d)
            val isCurrent = d == today
            val label = if (isCurrent) "امروز ($dayName ${p.day.toPersianDigits()} ${p.monthName})" else "$dayName ${p.day.toPersianDigits()} ${p.monthName}"
            Triple(d.toString(), label, isCurrent)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = { if (!isImporting) onDismiss() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, tint = SamadPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("دریافت ژتون از دانشجو / دوست", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mode Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = SamadPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("دریافت خودکار از سماد", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ثبت دستی کد", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (selectedTab == 0) {
                        // Automated Samad Fetch Mode
                        Text(
                            text = "مشخصات حساب سماد دوست خود را به همراه روزی که رزرو را می‌خواهید وارد کنید. فقط بارکد همان روز دریافت و ذخیره خواهد شد:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = friendUsername,
                            onValueChange = { friendUsername = it },
                            label = { Text("شماره دانشجویی / نام کاربری دوست") },
                            placeholder = { Text("مثال: 401123456") },
                            singleLine = true,
                            enabled = !isImporting,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = friendPassword,
                            onValueChange = { friendPassword = it },
                            label = { Text("رمز عبور سماد دوست") },
                            singleLine = true,
                            enabled = !isImporting,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "نمایش رمز"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("انتخاب روز مورد نظر برای دریافت رزرو:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectableDays.forEach { (dateStr, label, _) ->
                                val isSelected = chosenDate == dateStr
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { chosenDate = dateStr },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SamadPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SamadPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Text("انتخاب وعده (اختیاری):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                null to "همه وعده‌ها",
                                7 to "ناهار",
                                8 to "شام",
                                6 to "صبحانه"
                            ).forEach { (id, title) ->
                                val isSelected = chosenMealId == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { chosenMealId = id },
                                    label = { Text(title, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SamadPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = SamadPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "🔒 امنیت: مشخصات حساب دوست شما فقط برای خواندن بارکد همین یک روز به سرور سماد ارسال شده و در جایی ذخیره نمی‌شود.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Quick Paste from Messenger
                        val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clipboardText = remember {
                            try {
                                clipboardManager?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            } catch (e: Exception) { "" }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "یافتن و استخراج خودکار از پیام:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (clipboardText.isNotBlank()) {
                                TextButton(
                                    onClick = {
                                        // Auto-extract 9-digit code or digits from clipboard
                                        val digits = clipboardText.filter { it.isDigit() }
                                        if (digits.length >= 9) {
                                            forgotCode = digits.take(9)
                                        } else if (digits.isNotBlank()) {
                                            forgotCode = digits
                                        }
                                        // Attempt to extract food/meal/friend
                                        if (clipboardText.contains("شام")) {
                                            manualMealId = 8
                                            manualMealName = "شام"
                                        } else if (clipboardText.contains("ناهار")) {
                                            manualMealId = 7
                                            manualMealName = "ناهار"
                                        } else if (clipboardText.contains("صبحانه")) {
                                            manualMealId = 6
                                            manualMealName = "صبحانه"
                                        }
                                        val foodLine = clipboardText.lines().find { it.contains("غذا:") || it.contains("🍲") }
                                        if (foodLine != null) {
                                            foodName = foodLine.replace("🍲", "").replace("غذا:", "").trim()
                                        }
                                        val friendLine = clipboardText.lines().find { it.contains("فرستنده:") || it.contains("دانشجو:") || it.contains("👤") }
                                        if (friendLine != null) {
                                            friendName = friendLine.replace("👤", "").replace("فرستنده:", "").replace("دانشجو:", "").trim()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp), tint = SamadPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("جایگذاری از کلیپ‌بورد", fontSize = 11.sp, color = SamadPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = friendName,
                            onValueChange = { friendName = it },
                            label = { Text("نام دوست / صاحب ژتون") },
                            placeholder = { Text("مثال: علی رضایی") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("نام غذا *") },
                            placeholder = { Text("مثال: چلو خورشت قورمه سبزی") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = besideFoods,
                            onValueChange = { besideFoods = it },
                            label = { Text("مخلفات غذا (اختیاری)") },
                            placeholder = { Text("مثال: ماست، نوشابه، سالاد") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text("انتخاب وعده:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple(6, "صبحانه", "صبحانه"),
                                Triple(7, "ناهار", "ناهار"),
                                Triple(8, "شام", "شام")
                            ).forEach { (id, name, title) ->
                                val isSelected = manualMealId == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        manualMealId = id
                                        manualMealName = name
                                    },
                                    label = { Text(title, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SamadPrimary.copy(alpha = 0.15f),
                                        selectedLabelColor = SamadPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = forgotCode,
                            onValueChange = { if (it.length <= 15) forgotCode = it },
                            label = { Text("کد فراموشی یا بارکد ۹ رقمی *") },
                            placeholder = { Text("مثال: 987654321") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = selfName,
                            onValueChange = { selfName = it },
                            label = { Text("نام سلف") },
                            placeholder = { Text("مثال: سلف پردیس") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (selectedTab == 0) {
                    Button(
                        onClick = {
                            if (friendUsername.isNotBlank() && friendPassword.isNotBlank()) {
                                onFetchFromSamad(friendUsername.trim(), friendPassword.trim(), chosenDate, chosenMealId)
                            }
                        },
                        enabled = friendUsername.isNotBlank() && friendPassword.isNotBlank() && !isImporting,
                        colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("در حال استعلام...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("دریافت بارکد این روز", fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (foodName.isNotBlank() && forgotCode.isNotBlank()) {
                                onSaveManual(
                                    selectedDate,
                                    manualMealId,
                                    manualMealName,
                                    foodName.trim(),
                                    besideFoods.trim(),
                                    selfName.trim(),
                                    forgotCode.trim(),
                                    friendName.trim()
                                )
                            }
                        },
                        enabled = foodName.isNotBlank() && forgotCode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت ژتون", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, enabled = !isImporting) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun SyncReservesActionCard(
    isLoading: Boolean,
    isRefreshing: Boolean,
    lastSync: String?,
    onFetch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onFetch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SamadPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("fetch_reserves_button"),
                enabled = !isLoading && !isRefreshing
            ) {
                if (isLoading || isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "در حال دریافت از سماد...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دریافت مجدد ژتون‌ها (کش آفلاین)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (!lastSync.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "آخرین به‌روزرسانی: $lastSync",
                    fontSize = 11.sp,
                    color = SamadOnSurfaceVariant
                )
            }
        }
    }
}

/**
 * Banner notifying student when an active reservation is in a dining hall
 * different from their preferred cafeteria.
 */
@Composable
private fun DifferentSelfWarningBanner(
    warningText: String,
    onSettingsClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        ),
        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("different_self_warning_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFECB3)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = warningText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C)
                )
                Text(
                    text = "برای تغییر سلف پیش‌فرض یا تنظیم هشدار، به بخش تنظیمات بروید.",
                    fontSize = 10.sp,
                    color = Color(0xFFE65100).copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            TextButton(
                onClick = onSettingsClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "تنظیمات",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

/**
 * Banner on Home screen announcing a new GitHub Release with direct download/install action.
 */
@Composable
private fun HomeUpdateBanner(
    update: UpdateCheckResult.UpdateAvailable,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: (com.example.data.model.GitHubAsset?) -> Unit,
    onOpenBrowser: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        border = BorderStroke(1.2.dp, SamadPrimary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_update_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SamadPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "بروزرسانی جدید: نسخه ${update.newVersion}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SamadOnSurface
                        )
                        update.apkAsset?.formattedSize?.takeIf { it.isNotBlank() }?.let { sizeStr ->
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SamadPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sizeStr,
                                    fontSize = 10.sp,
                                    color = SamadPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Text(
                        text = update.changelog.lines().firstOrNull { it.isNotBlank() } ?: "رفع باگ‌ها و بهبود عملکرد",
                        fontSize = 11.sp,
                        color = SamadOnSurfaceVariant,
                        maxLines = 2
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = SamadOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isDownloading) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SamadPrimary,
                    trackColor = Color(0xFFC8E6C9)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "در حال دانلود فایل نصب... ${(downloadProgress * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = SamadPrimary,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onOpenBrowser,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صفحه گیت‌هاب", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onDownloadClick(update.apkAsset) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SamadPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دانلود و نصب مستقیم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

