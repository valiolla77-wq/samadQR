package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PendingReserveEntity
import com.example.data.model.ProgramFoodItem
import com.example.data.model.SelfItem
import com.example.ui.components.AmbientOrbsScaffold
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.util.MealTimeHelper
import com.example.util.PersianDateUtil
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onNavigateBack: () -> Unit
) {
    androidx.activity.compose.BackHandler(onBack = onNavigateBack)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSelfSelectionDialogOpen by remember { mutableStateOf(false) }

    // Load initial reservation data if empty
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            viewModel.loadReservationSelfsAndPrograms(forceRefresh = false)
        }
    }

    // Display action messages or errors via snackbar
    LaunchedEffect(uiState.reserveActionMessage) {
        uiState.reserveActionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearReserveActionMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmbientOrbsScaffold {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "رزرو غذا و انتخاب سلف",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "طبق برنامه و وعده‌های فعال سلف",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.testTag("reservation_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بازگشت"
                                )
                            }
                        },
                        actions = {
                            // Live Wallet Credit Pill
                            WalletCreditPill(
                                creditRials = uiState.userCredit,
                                isLoading = uiState.isCreditLoading,
                                onRefresh = { viewModel.loadLiveCredit() }
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                PullToRefreshBox(
                    isRefreshing = uiState.isReservationLoading,
                    onRefresh = { viewModel.loadReservationSelfsAndPrograms(forceRefresh = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .testTag("reservation_pull_refresh")
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        // 1. Dining Hall Selector & Confirmation Card
                        item {
                            DiningHallSelectionCard(
                                selectedSelf = uiState.selectedReservationSelf,
                                availableSelfs = uiState.availableSelfs,
                                onOpenDialog = { isSelfSelectionDialogOpen = true }
                            )
                        }

                        // 2. Pending Reserves Shelf ("در انتظار شارژ و تأیید")
                        if (uiState.pendingReserves.isNotEmpty()) {
                            item {
                                PendingReservesSection(
                                    pendingReserves = uiState.pendingReserves,
                                    isSubmitting = uiState.isSubmittingReserve,
                                    onConfirm = { pending ->
                                        viewModel.confirmAndExecutePendingReserve(pending)
                                    },
                                    onCancel = { programId ->
                                        viewModel.cancelPendingReserve(programId)
                                    }
                                )
                            }
                        }

                        // 3. Week Selector Tabs (این هفته / هفته آینده)
                        item {
                            WeekSelectorTabs(
                                selectedOffset = uiState.selectedWeekOffset,
                                onSelectOffset = { offset ->
                                    viewModel.selectReservationWeekOffset(offset)
                                }
                            )
                        }

                        // 4. Days of Week Selector
                        item {
                            DaysOfWeekScrollRow(
                                weekOffset = uiState.selectedWeekOffset,
                                selectedDate = uiState.selectedReservationDate,
                                onSelectDate = { date ->
                                    viewModel.selectReservationDate(date)
                                }
                            )
                        }

                        // 5. Food List for Selected Date
                        val cleanSelectedDate = uiState.selectedReservationDate.trim().split(" ")[0].replace("/", "-")
                        val programsForDate = uiState.weeklyReservationPrograms.filter {
                            val cleanItemDate = it.date.trim().split(" ")[0].replace("/", "-")
                            cleanItemDate == cleanSelectedDate
                        }

                        if (uiState.isReservationLoading && programsForDate.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                                }
                            }
                        } else if (programsForDate.isEmpty()) {
                            item {
                                EmptyProgramsNotice(
                                    selectedDate = uiState.selectedReservationDate,
                                    selfName = uiState.selectedReservationSelf?.name ?: "سلف"
                                )
                            }
                        } else {
                            items(programsForDate, key = { it.programId }) { food ->
                                val isAlreadyReserved = uiState.reservedProgramIds.contains(food.programId)
                                val isPending = uiState.pendingReserves.any { it.programId == food.programId }

                                FoodProgramCard(
                                    food = food,
                                    isReserved = isAlreadyReserved,
                                    isPending = isPending,
                                    onReserveClick = {
                                        viewModel.onFoodClickForReservation(food)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Dining Hall Selection
    if (isSelfSelectionDialogOpen) {
        SelfSelectionDialog(
            currentSelf = uiState.selectedReservationSelf,
            availableSelfs = uiState.availableSelfs,
            onSelect = { self ->
                viewModel.selectReservationSelf(self)
                isSelfSelectionDialogOpen = false
            },
            onDismiss = { isSelfSelectionDialogOpen = false }
        )
    }

    // Dialog: Invoice Confirmation (موجودی کافی)
    if (uiState.isInvoiceDialogOpen && uiState.selectedFoodForInvoice != null) {
        ReservationInvoiceDialog(
            food = uiState.selectedFoodForInvoice,
            selfName = uiState.selectedReservationSelf?.name ?: "سلف دانشگاه",
            userCredit = uiState.userCredit,
            isSubmitting = uiState.isSubmittingReserve,
            onConfirm = {
                viewModel.executeDirectReserveFromInvoice(uiState.selectedFoodForInvoice)
            },
            onDismiss = { viewModel.closeInvoiceDialog() }
        )
    }

    // Dialog: Deficit & Pending Stage (کسری موجودی)
    if (uiState.isDeficitDialogOpen && uiState.selectedFoodForInvoice != null) {
        DeficitRechargeDialog(
            food = uiState.selectedFoodForInvoice,
            selfName = uiState.selectedReservationSelf?.name ?: "سلف دانشگاه",
            deficitAmount = uiState.invoiceDeficitAmount,
            userCredit = uiState.userCredit,
            onStagePending = {
                viewModel.stagePendingReserve(
                    uiState.selectedFoodForInvoice,
                    uiState.invoiceDeficitAmount
                )
            },
            onDismiss = { viewModel.closeDeficitDialog() }
        )
    }
}

// =========================================================================
// UI Component Implementations
// =========================================================================

@Composable
fun WalletCreditPill(
    creditRials: Long,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    val tomans = creditRials / 10L
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "${PersianDateUtil.toPersianDigits(String.format("%,d", tomans))} تومان",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "موجودی کیف پول",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "بروزرسانی موجودی",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DiningHallSelectionCard(
    selectedSelf: SelfItem?,
    availableSelfs: List<SelfItem>,
    onOpenDialog: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDialog)
            .testTag("dining_hall_selector_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "سلف سرویس انتخابی برای رزرو",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedSelf?.name ?: "در حال بارگذاری...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "تغییر سلف",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun PendingReservesSection(
    pendingReserves: List<PendingReserveEntity>,
    isSubmitting: Boolean,
    onConfirm: (PendingReserveEntity) -> Unit,
    onCancel: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1).copy(alpha = 0.95f) // Warm amber container
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_reserves_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.HourglassTop,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "رزرو در انتظار شارژ و تأیید (${PersianDateUtil.toPersianDigits(pendingReserves.size.toString())})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C)
                )
            }

            Text(
                text = "پس از انجام شارژ در سامانه دانشگاه، دکمه «تأیید پرداخت و ثبت نهایی» را لمس کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            pendingReserves.forEach { pending ->
                PendingReserveItemRow(
                    pending = pending,
                    isSubmitting = isSubmitting,
                    onConfirm = { onConfirm(pending) },
                    onCancel = { onCancel(pending.programId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun PendingReserveItemRow(
    pending: PendingReserveEntity,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val deficitTomans = pending.deficitAmount / 10L
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pending.foodName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "${pending.mealTypeName} • ${pending.selfName} • ${pending.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828)
                ) {
                    Text(
                        text = "کسری: ${PersianDateUtil.toPersianDigits(String.format("%,d", deficitTomans))} تومان",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_pending_${pending.programId}")
                ) {
                    Text(
                        text = "انصراف",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onConfirm,
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_pending_${pending.programId}")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "تأیید پرداخت و ثبت نهایی",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekSelectorTabs(
    selectedOffset: Int,
    onSelectOffset: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedOffset,
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Tab(
            selected = selectedOffset == 0,
            onClick = { onSelectOffset(0) },
            text = {
                Text(
                    text = "هفته جاری",
                    fontWeight = if (selectedOffset == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
        Tab(
            selected = selectedOffset == 1,
            onClick = { onSelectOffset(1) },
            text = {
                Text(
                    text = "هفته آینده",
                    fontWeight = if (selectedOffset == 1) FontWeight.Bold else FontWeight.Normal
                )
            }
        )
    }
}

@Composable
fun DaysOfWeekScrollRow(
    weekOffset: Int,
    selectedDate: String,
    onSelectDate: (String) -> Unit
) {
    val targetBase = LocalDate.now().plusWeeks(weekOffset.toLong())
    val saturday = PersianDateUtil.getWeekStartSaturday(targetBase)
    val days = (0..6).map { saturday.plusDays(it.toLong()) }

    val selectedIndex = days.indexOfFirst { it.toString() == selectedDate }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        days.forEachIndexed { index, date ->
            val dateStr = date.toString()
            val isSelected = dateStr == selectedDate
            val persianDate = PersianDateUtil.gregorianToPersian(date)
            val dayName = PersianDateUtil.getDayOfWeekPersian(date)

            Tab(
                selected = isSelected,
                onClick = { onSelectDate(dateStr) },
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = PersianDateUtil.toPersianDigits("${persianDate.day} ${persianDate.monthName}"),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodProgramCard(
    food: ProgramFoodItem,
    isReserved: Boolean,
    isPending: Boolean,
    onReserveClick: () -> Unit
) {
    val mealName = MealTimeHelper.MealCategory.getMealTypeName(food.mealTypeId)
    val priceTomans = food.price / 10L

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isReserved -> Color(0xFFE8F5E9)
                isPending -> Color(0xFFFFF8E1)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isReserved -> Color(0xFF81C784)
                isPending -> Color(0xFFFFD54F)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isReserved && !food.reserveRuleViolated) { onReserveClick() }
            .testTag("food_card_${food.programId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Meal Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = mealName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Price Tag
                Text(
                    text = "${PersianDateUtil.toPersianDigits(String.format("%,d", priceTomans))} تومان",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Food Title
            Text(
                text = food.foodName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status or Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (food.reserveRuleViolated) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مهلت رزرو این وعده به پایان رسیده است",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                when {
                    isReserved -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رزرو شده",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    isPending -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF57F17),
                            contentColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.HourglassTop,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "در انتظار تأیید",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    else -> {
                        Button(
                            onClick = onReserveClick,
                            enabled = !food.reserveRuleViolated,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("reserve_btn_${food.programId}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "انتخاب و رزرو",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProgramsNotice(
    selectedDate: String,
    selfName: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "برنامه غذایی فعالی برای این روز تعریف نشده است",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "در تاریخ $selectedDate برای $selfName طبق وعده‌های فعال سلف غذایی موجود نیست.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =========================================================================
// Dialogs
// =========================================================================

@Composable
fun SelfSelectionDialog(
    currentSelf: SelfItem?,
    availableSelfs: List<SelfItem>,
    onSelect: (SelfItem) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "انتخاب سلف سرویس",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "لطفاً سلف سرویسی که می‌خواهید برنامه غذایی آن را مشاهده و رزرو نمایید انتخاب کنید:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                availableSelfs.forEach { self ->
                    val isSelected = self.id == currentSelf?.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(self) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = self.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بستن")
            }
        }
    )
}

@Composable
fun ReservationInvoiceDialog(
    food: ProgramFoodItem,
    selfName: String,
    userCredit: Long,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val mealName = MealTimeHelper.MealCategory.getMealTypeName(food.mealTypeId)
    val priceTomans = food.price / 10L
    val creditTomans = userCredit / 10L
    val remainingTomans = (userCredit - food.price) / 10L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ReceiptLong(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "پیش‌فاکتور رزرو غذا",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dining hall confirmation banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "سلف سرویس تأیید شده:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = selfName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Food details
                InvoiceRow("نام غذا", food.foodName)
                InvoiceRow("وعده غذایی", mealName)
                InvoiceRow("تاریخ تحویل", food.date)
                InvoiceRow("مبلغ رزرو", "${PersianDateUtil.toPersianDigits(String.format("%,d", priceTomans))} تومان")
                InvoiceRow("موجودی فعلی کیف پول", "${PersianDateUtil.toPersianDigits(String.format("%,d", creditTomans))} تومان")
                InvoiceRow("موجودی پس از کسر", "${PersianDateUtil.toPersianDigits(String.format("%,d", remainingTomans))} تومان")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSubmitting,
                modifier = Modifier.testTag("confirm_invoice_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("تأیید و ثبت قطعی رزرو")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun DeficitRechargeDialog(
    food: ProgramFoodItem,
    selfName: String,
    deficitAmount: Long,
    userCredit: Long,
    onStagePending: () -> Unit,
    onDismiss: () -> Unit
) {
    val mealName = MealTimeHelper.MealCategory.getMealTypeName(food.mealTypeId)
    val priceTomans = food.price / 10L
    val creditTomans = userCredit / 10L
    val deficitTomans = deficitAmount / 10L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFF57F17),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "کسری موجودی برای رزرو",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dining hall confirmation banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "سلف سرویس انتخابی:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selfName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                InvoiceRow("نام غذا", food.foodName)
                InvoiceRow("وعده و تاریخ", "$mealName • ${food.date}")
                InvoiceRow("قیمت غذا", "${PersianDateUtil.toPersianDigits(String.format("%,d", priceTomans))} تومان")
                InvoiceRow("موجودی فعلی کیف پول", "${PersianDateUtil.toPersianDigits(String.format("%,d", creditTomans))} تومان")

                // Deficit Highlight Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "مبلغ مورد نیاز برای شارژ در سامانه:",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${PersianDateUtil.toPersianDigits(String.format("%,d", deficitTomans))} تومان",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            text = "(${PersianDateUtil.toPersianDigits(String.format("%,d", deficitAmount))} ریال)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                Text(
                    text = "💡 راهنما: می‌توانید این رزرو را به عنوان «در انتظار شارژ» ذخیره کنید. سپس به سامانه اصلی دانشگاه مراجعه کرده و حساب خود را شارژ نمایید. پس از بازگشت به همین بخش، با لمس دکمه تأیید، رزرو شما بلافاصله ثبت خواهد شد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onStagePending,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF57F17)
                ),
                modifier = Modifier.testTag("stage_pending_reserve_button")
            ) {
                Text(
                    text = "ذخیره در انتظار شارژ و تأیید",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Icon helper function for Receipt icon without extra dependency
@Composable
fun Icons.Filled.ReceiptLong(): androidx.compose.ui.graphics.vector.ImageVector = Icons.Filled.DateRange
