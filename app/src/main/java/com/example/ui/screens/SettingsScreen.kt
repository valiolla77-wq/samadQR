package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.BuildConfig
import com.example.util.UpdateCheckResult
import com.example.util.UpdateManager
import com.example.util.toPersianDigits
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.SecurePrefs
import com.example.ui.components.AmbientOrbsScaffold
import com.example.ui.components.SamadBrandTitle
import com.example.ui.theme.SamadBackground
import com.example.ui.theme.SamadOnSurface
import com.example.ui.theme.SamadOnSurfaceVariant
import com.example.ui.theme.SamadOutline
import com.example.ui.theme.SamadPrimary
import com.example.ui.theme.SamadSecondary
import com.example.ui.theme.SamadSurfaceBright
import com.example.ui.theme.SamadSurfaceContainerLow
import com.example.ui.theme.SamadSurfaceContainerLowest
import com.example.ui.theme.StatusExpired
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.widget.SamadFoodWidgetProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onNavigateBack: () -> Unit,
    onLogoutDone: () -> Unit
) {
    val context = LocalContext.current
    val prefs = viewModel.prefs

    var baseUrlInput by remember { mutableStateOf(prefs.baseUrl) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditGithubRepoDialog by remember { mutableStateOf(false) }
    var tempGithubRepo by remember { mutableStateOf(uiState.githubRepo) }
    var showEditCustomSelfDialog by remember { mutableStateOf(false) }
    var tempCustomSelf by remember { mutableStateOf("") }

    var breakfastStart by remember { mutableStateOf(prefs.breakfastStart) }
    var breakfastEnd by remember { mutableStateOf(prefs.breakfastEnd) }
    var lunchStart by remember { mutableStateOf(prefs.lunchStart) }
    var lunchEnd by remember { mutableStateOf(prefs.lunchEnd) }
    var dinnerStart by remember { mutableStateOf(prefs.dinnerStart) }
    var dinnerEnd by remember { mutableStateOf(prefs.dinnerEnd) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmbientOrbsScaffold {
            Scaffold(
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back squircle button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "تنظیمات سامانه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Empty spacer or brand badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "QR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = SamadPrimary
                            )
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Bento 1: User Profile Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SamadPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "مشخصات دانشجو",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "اطلاعات ثبت‌شده در پرتال تغذیه سماد",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SettingInfoRow("نام و نام خانوادگی", uiState.studentName ?: "ثبت نشده")
                            SettingInfoRow("شماره دانشجویی / کاربری", uiState.studentNumber ?: prefs.username ?: "ثبت نشده")
                            if (!uiState.studentMajor.isNullOrBlank()) {
                                SettingInfoRow("رشته تحصیلی", uiState.studentMajor)
                            }
                        }
                    }

                    // Bento: Dark Mode Theme Card (Item 1 requirement)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "حالت تاریک (دارک مود)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isDarkMode) "تم تیره فعال است (کاهش مصرف باتری و نور صفحه)" else "تم روشن فعال است",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = uiState.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SamadPrimary
                                )
                            )
                        }
                    }

                    // Bento 2: Push Notifications & Sync Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1F006B2C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (uiState.isDarkMode) Color(0xFF33230A) else Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = SamadSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "اعلان یادآور وعده غذا",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SamadOnSurface
                                        )
                                        Text(
                                            text = "ارسال اعلان ۳۰ دقیقه قبل از شروع زمان سرو غذا",
                                            fontSize = 11.sp,
                                            color = SamadOnSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.remindersEnabled,
                                    onCheckedChange = { viewModel.toggleReminders(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SamadPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Auto-sync daily switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = SamadPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "به‌روزرسانی خودکار روزانه",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SamadOnSurface
                                        )
                                        Text(
                                            text = "دریافت خودکار برنامه غذایی در ساعت ۱۱:۰۰ و ۱۸:۰۰",
                                            fontSize = 11.sp,
                                            color = SamadOnSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.autoSyncDaily,
                                    onCheckedChange = { viewModel.toggleAutoSync(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SamadPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Auto-sync on app launch switch (User requirement 1)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = SamadPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "دریافت خودکار هنگام ورود به برنامه",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SamadOnSurface
                                        )
                                        Text(
                                            text = if (uiState.autoSyncOnLaunch)
                                                "دریافت خودکار رزروها در زمان باز کردن برنامه"
                                            else
                                                "خاموش (دریافت فقط با کلیک روی دکمه به‌روزرسانی دستی)",
                                            fontSize = 11.sp,
                                            color = SamadOnSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.autoSyncOnLaunch,
                                    onCheckedChange = { viewModel.setAutoSyncOnLaunch(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SamadPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = { viewModel.clearCacheAndRefresh() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("clear_cache_and_refresh_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SamadPrimary
                                ),
                                border = BorderStroke(1.dp, SamadPrimary.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "پاکسازی و به‌روزرسانی امن کش از سرور",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "توجه: پاکسازی و جایگزینی کش مشروط به برقراری ارتباط با سامانه است؛ در صورت قطعی سرور یا اینترنت، بارکدها و اطلاعات ذخیره‌شده قبلی شما کاملاً محفوظ می‌ماند.",
                                fontSize = 10.sp,
                                color = SamadOnSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp)
                            )
                        }
                    }

                    // Bento: Firebase Cloud Messaging (FCM) Meal Reminders Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = SamadPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "یادآور ابری فایربیس (FCM)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ارسال اعلان هشدار به گوشی پیش از اتمام وقت توزیع غذا",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = uiState.fcmEnabled,
                                    onCheckedChange = { viewModel.toggleFcm(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SamadPrimary
                                    )
                                )
                            }

                            if (uiState.fcmEnabled) {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Active topics badges
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "کانال‌های دریافت اعلان رزرو:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(
                                            "همگانی سلف" to true,
                                            "ناهار" to uiState.cafeteriaLunchEnabled,
                                            "شام" to uiState.cafeteriaDinnerEnabled,
                                            "صبحانه" to uiState.cafeteriaBreakfastEnabled,
                                            "سحری" to uiState.cafeteriaSuhurEnabled,
                                            "افطار" to uiState.cafeteriaIftarEnabled
                                        ).forEach { (label, active) ->
                                            if (active) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(SamadPrimary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "• $label",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SamadPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // FCM Device Token Section
                                val token = uiState.fcmToken
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "شناسه FCM این دستگاه:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!token.isNullOrBlank()) {
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("FCM Token", token))
                                                    Toast.makeText(context, "توکن FCM کپی شد", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "کپی توکن",
                                                    tint = SamadPrimary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (!token.isNullOrBlank()) {
                                            "${token.take(24)}...${token.takeLast(16)}"
                                        } else {
                                            "در انتظار دریافت شناسه از سرویس گوگل..."
                                        },
                                        fontSize = 10.sp,
                                        color = if (!token.isNullOrBlank()) SamadPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Test Notification Button
                                OutlinedButton(
                                    onClick = { viewModel.sendTestMealNotification() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ارسال اعلان تستی یادآور غذا 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Bento: Version Control Card (User requirement 8)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(SamadPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SystemUpdate,
                                            contentDescription = null,
                                            tint = SamadPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "کنترل و وضعیت نسخه نرم‌افزار",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "نسخه ۱.۲.۰ (Build 2) - پایدار",
                                            fontSize = 11.sp,
                                            color = SamadPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SamadPrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "به‌روز",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SamadPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // GitHub Repo info & config row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (uiState.isDarkMode) Color(0xFF1E2822) else Color(0xFFF1F8F4))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "مخزن انتشارات در گیت‌هاب (GitHub Releases):",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                    Text(
                                        text = uiState.githubRepo,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SamadPrimary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        tempGithubRepo = uiState.githubRepo
                                        showEditGithubRepoDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ویرایش مخزن",
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Auto-check updates toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "بررسی خودکار هنگام باز شدن برنامه",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "در صورت وجود نسخه جدیدتر در گیت‌هاب، فوراً مطلع شوید",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.autoCheckUpdates,
                                    onCheckedChange = { viewModel.setAutoCheckUpdates(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SamadPrimary)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Check update button
                            Button(
                                onClick = { viewModel.checkForUpdates(manual = true) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary),
                                enabled = !uiState.isCheckingUpdate
                            ) {
                                if (uiState.isCheckingUpdate) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("در حال بررسی گیت‌هاب...", fontSize = 12.sp, color = Color.White)
                                } else {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بررسی بروزرسانی در GitHub Releases", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Dynamic Update Result Status
                            val updateResult = uiState.updateCheckResult
                            if (updateResult is UpdateCheckResult.UpdateAvailable) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    border = BorderStroke(1.dp, SamadPrimary.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🎉 نسخه ${updateResult.newVersion} در دسترس است!",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = SamadPrimary
                                            )
                                            updateResult.apkAsset?.formattedSize?.takeIf { it.isNotBlank() }?.let {
                                                Text(text = it, fontSize = 11.sp, color = SamadOnSurfaceVariant)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = updateResult.changelog,
                                            fontSize = 11.sp,
                                            color = SamadOnSurfaceVariant,
                                            maxLines = 4
                                        )

                                        if (uiState.isDownloadingUpdate) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            LinearProgressIndicator(
                                                progress = { uiState.downloadProgress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = SamadPrimary,
                                                trackColor = Color(0xFFC8E6C9)
                                            )
                                            Text(
                                                text = "در حال دانلود... ${(uiState.downloadProgress * 100).toInt()}%",
                                                fontSize = 10.sp,
                                                color = SamadPrimary
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                OutlinedButton(
                                                    onClick = { UpdateManager.openInBrowser(context, updateResult.release.htmlUrl) },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("گیت‌هاب", fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(
                                                    onClick = {
                                                        val asset = updateResult.apkAsset
                                                        if (asset != null) {
                                                            viewModel.downloadAndInstallUpdate(context, updateResult.release, asset)
                                                        } else {
                                                            UpdateManager.openInBrowser(context, updateResult.release.htmlUrl)
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("دانلود و نصب مستقیم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (updateResult is UpdateCheckResult.UpToDate) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SamadPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("شما از آخرین نسخه منتشرشده در گیت‌هاب استفاده می‌کنید.", fontSize = 11.sp, color = SamadPrimary)
                                }
                            } else if (updateResult is UpdateCheckResult.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = StatusExpired, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(updateResult.message, fontSize = 11.sp, color = StatusExpired)
                                }
                            }
                        }
                    }

                    // Bento: Student Preferred Cafeteria Selection Card (سلف انتخابی دانشجو)
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1F006B2C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("preferred_cafeteria_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "سلف انتخابی دانشجو (سلف من)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "تعیین سلف اصلی جهت سهولت و نمایش هشدار در صورت ثبت اشتباه در سلف دیگر",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "سلف پیش‌فرض شما:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SamadOnSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Cafeteria selector chips
                            val availableSelfs = (listOf("همه سلف‌ها (بدون فیلتر)") + uiState.knownSelfNames.toList()).distinct()
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = uiState.preferredSelfName == null,
                                        onClick = { viewModel.setPreferredSelf(null) },
                                        label = { Text("همه سلف‌ها", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SamadPrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )

                                    uiState.knownSelfNames.take(2).forEach { selfName ->
                                        FilterChip(
                                            selected = uiState.preferredSelfName == selfName,
                                            onClick = { viewModel.setPreferredSelf(selfName) },
                                            label = { Text(selfName, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SamadPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                if (uiState.knownSelfNames.size > 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.knownSelfNames.drop(2).take(3).forEach { selfName ->
                                            FilterChip(
                                                selected = uiState.preferredSelfName == selfName,
                                                onClick = { viewModel.setPreferredSelf(selfName) },
                                                label = { Text(selfName, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = SamadPrimary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                // Custom self input button
                                OutlinedButton(
                                    onClick = {
                                        tempCustomSelf = uiState.preferredSelfName ?: ""
                                        showEditCustomSelfDialog = true
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.preferredSelfName != null) "سلف انتخابی: ${uiState.preferredSelfName}" else "انتخاب یا نوشتن نام سلف دلخواه...",
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Notify if reservation is in another cafeteria
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "هشدار رزرو در سلف مغایر",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "اگر غذایی در سلفی غیر از سلف انتخابی رزرو شده باشد، در صفحه اصلی هشدار داده شود.",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.notifyDifferentSelf,
                                    onCheckedChange = { viewModel.setNotifyDifferentSelf(it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SamadPrimary)
                                )
                            }
                        }
                    }

                    // Bento 3: Cafeteria Meal Configuration Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1F006B2C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fastfood,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "وعده‌های فعال سلف دانشگاه",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "ناهار و شام به‌صورت پیش‌فرض فعالند. سایر وعده‌ها تنها در صورت فعال‌سازی یا داشتن رزرو فعال نمایش می‌یابند.",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Lunch (Main)
                            MealSwitchRow(
                                title = "ناهار (وعده اصلی)",
                                subtitle = "وعده ظهر دانشگاه • فعال به‌صورت پیش‌فرض",
                                checked = uiState.cafeteriaLunchEnabled,
                                onCheckedChange = { viewModel.setCafeteriaMealEnabled("lunch", it) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Dinner (Main)
                            MealSwitchRow(
                                title = "شام (وعده اصلی)",
                                subtitle = "وعده شب خوابگاه یا سلف • فعال به‌صورت پیش‌فرض",
                                checked = uiState.cafeteriaDinnerEnabled,
                                onCheckedChange = { viewModel.setCafeteriaMealEnabled("dinner", it) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Breakfast
                            MealSwitchRow(
                                title = "صبحانه",
                                subtitle = "مخصوص خوابگاه‌ها یا سلف‌های ارائه‌دهنده صبحانه",
                                checked = uiState.cafeteriaBreakfastEnabled,
                                onCheckedChange = { viewModel.setCafeteriaMealEnabled("breakfast", it) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Suhur
                            MealSwitchRow(
                                title = "سحری",
                                subtitle = "ویژه ماه مبارک رمضان",
                                checked = uiState.cafeteriaSuhurEnabled,
                                onCheckedChange = { viewModel.setCafeteriaMealEnabled("suhur", it) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Iftar
                            MealSwitchRow(
                                title = "افطاری",
                                subtitle = "ویژه ماه مبارک رمضان",
                                checked = uiState.cafeteriaIftarEnabled,
                                onCheckedChange = { viewModel.setCafeteriaMealEnabled("iftar", it) }
                            )
                        }
                    }

                    // Bento 4: App Widget Pin Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("widget_pin_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Widgets,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ویجت هوشمند صفحه اصلی",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "دسترسی فوری به بارکد و ژتون غذای روز در صفحه اصلی",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    SamadFoodWidgetProvider.requestPinWidget(context)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SamadPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("pin_widget_button")
                            ) {
                                Icon(imageVector = Icons.Default.AddHome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("افزودن ویجت به صفحه اصلی گوشی", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Bento 5: Serving Hours Editor Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1F006B2C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ساعت‌های محلی سرو غذا در سلف",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "برای تغییر، روی ساعت کلیک کنید",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            MealHoursRow("صبحانه", breakfastStart, breakfastEnd, onStart = { breakfastStart = it }, onEnd = { breakfastEnd = it })
                            MealHoursRow("ناهار", lunchStart, lunchEnd, onStart = { lunchStart = it }, onEnd = { lunchEnd = it })
                            MealHoursRow("شام", dinnerStart, dinnerEnd, onStart = { dinnerStart = it }, onEnd = { dinnerEnd = it })

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    prefs.breakfastStart = breakfastStart
                                    prefs.breakfastEnd = breakfastEnd
                                    prefs.lunchStart = lunchStart
                                    prefs.lunchEnd = lunchEnd
                                    prefs.dinnerStart = dinnerStart
                                    prefs.dinnerEnd = dinnerEnd
                                    Toast.makeText(context, "ساعت‌های سرو با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ذخیره ساعت‌های سرو", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Bento 6: Base URL Server Config
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1F006B2C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (uiState.isDarkMode) Color(0xFF1F3226) else Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "آدرس سرور سامانه سماد",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SamadOnSurface
                                    )
                                    Text(
                                        text = "پیش‌فرض دانشگاه: https://saba.tvu.ac.ir/",
                                        fontSize = 11.sp,
                                        color = SamadOnSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = baseUrlInput,
                                onValueChange = { baseUrlInput = it },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SamadPrimary,
                                    unfocusedBorderColor = Color(0x2B006B2C),
                                    focusedContainerColor = SamadSurfaceBright,
                                    unfocusedContainerColor = SamadSurfaceBright,
                                    cursorColor = SamadPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("base_url_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.updateBaseUrl(baseUrlInput)
                                        Toast.makeText(context, "آدرس سرور ذخیره شد", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ذخیره آدرس")
                                }

                                OutlinedButton(
                                    onClick = {
                                        baseUrlInput = SecurePrefs.DEFAULT_BASE_URL
                                        viewModel.updateBaseUrl(SecurePrefs.DEFAULT_BASE_URL)
                                        Toast.makeText(context, "بازنشانی شد", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SamadOnSurfaceVariant)
                                ) {
                                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("پیش‌فرض")
                                }
                            }
                        }
                    }

                    // Bento 7: Logout Button
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLowest),
                        border = BorderStroke(1.dp, Color(0x1FDC2626)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Button(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = StatusExpired,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("logout_button")
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("خروج از حساب کاربری و پاکسازی اطلاعات", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showLogoutDialog) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("خروج از حساب کاربری", fontWeight = FontWeight.Bold) },
                text = { Text("آیا مطمئن هستید؟ تمام توکن‌ها، رمز عبور ذخیره شده و رزروهای کش شده از دستگاه پاک خواهند شد.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            onLogoutDone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusExpired),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("خروج کامل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("انصراف")
                    }
                },
                containerColor = SamadSurfaceContainerLowest,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    if (showEditGithubRepoDialog) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { showEditGithubRepoDialog = false },
                title = { Text("تنظیم مخزن انتشارات گیت‌هاب", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "نام کاربری و نام مخزن گیت‌هاب جهت دریافت آپدیت‌ها و فایل APK امضاشده (مثال: owner/repo):",
                            fontSize = 12.sp,
                            color = SamadOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempGithubRepo,
                            onValueChange = { tempGithubRepo = it },
                            placeholder = { Text("owner/repo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempGithubRepo.isNotBlank()) {
                                viewModel.setGithubRepo(tempGithubRepo)
                                showEditGithubRepoDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره مخزن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditGithubRepoDialog = false }) {
                        Text("انصراف")
                    }
                },
                containerColor = SamadSurfaceContainerLowest,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    if (showEditCustomSelfDialog) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { showEditCustomSelfDialog = false },
                title = { Text("انتخاب سلف دلخواه", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            text = "نام سلف پیش‌فرض خود را وارد کنید (مثال: سلف مرکزی، خوابگاه برادران، سلف ۲):",
                            fontSize = 12.sp,
                            color = SamadOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempCustomSelf,
                            onValueChange = { tempCustomSelf = it },
                            placeholder = { Text("نام سلف...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempCustomSelf.isNotBlank()) {
                                viewModel.setPreferredSelf(tempCustomSelf)
                            } else {
                                viewModel.setPreferredSelf(null)
                            }
                            showEditCustomSelfDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SamadPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت سلف")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditCustomSelfDialog = false }) {
                        Text("انصراف")
                    }
                },
                containerColor = SamadSurfaceContainerLowest,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = SamadOnSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SamadOnSurface)
    }
}

@Composable
private fun MealSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = SamadOnSurface)
            Text(subtitle, fontSize = 11.sp, color = SamadOnSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SamadPrimary
            )
        )
    }
}

@Composable
private fun MealHoursRow(
    title: String,
    start: String,
    end: String,
    onStart: (String) -> Unit,
    onEnd: (String) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = SamadOnSurface,
            modifier = Modifier.width(70.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeBadgeChip(time = start) {
                showNativeTimePicker(context, start, onStart)
            }
            Text(
                text = " تا ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SamadOnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            TimeBadgeChip(time = end) {
                showNativeTimePicker(context, end, onEnd)
            }
        }
    }
}

@Composable
private fun TimeBadgeChip(
    time: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = SamadSurfaceBright,
        border = BorderStroke(1.dp, SamadPrimary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = SamadPrimary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = time.ifBlank { "00:00" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamadOnSurface
                )
            }
        }
    }
}

private fun showNativeTimePicker(
    context: Context,
    currentTime: String,
    onTimeSelected: (String) -> Unit
) {
    val parts = currentTime.split(":")
    val initH = parts.getOrNull(0)?.toIntOrNull() ?: 12
    val initM = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val picker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formatted = String.format(java.util.Locale.US, "%02d:%02d", hourOfDay, minute)
            onTimeSelected(formatted)
        },
        initH,
        initM,
        true
    )
    picker.show()
}
