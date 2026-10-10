package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.CachedReserveEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.model.SharedTokenItem
import com.example.data.model.TokenPacket
import com.example.ui.components.AmbientOrbsScaffold
import com.example.ui.theme.SamadOnSurface
import com.example.ui.theme.SamadOnSurfaceVariant
import com.example.ui.theme.SamadPrimary
import com.example.ui.theme.SamadSecondary
import com.example.ui.theme.SamadSurfaceBright
import com.example.ui.theme.SamadSurfaceContainerLow
import com.example.ui.theme.SamadSurfaceContainerLowest
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.util.QrCodeGenerator
import com.example.util.toPersianDigits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: همه, 1: دریافتی, 2: ارسالی
    var showSendDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var fullBarcodeForgotCode by remember { mutableStateOf<String?>(null) }
    var fullBarcodeTitle by remember { mutableStateOf("") }

    val filteredMessages = remember(uiState.messengerMessages, selectedTab) {
        when (selectedTab) {
            1 -> uiState.messengerMessages.filter { !it.isOutgoing }
            2 -> uiState.messengerMessages.filter { it.isOutgoing }
            else -> uiState.messengerMessages
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmbientOrbsScaffold {
            Scaffold(
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restaurant,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "پیام‌رسان ژتون سماد",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (uiState.messengerRelayServer == "cloudflare") Color(0xFF10B981) else Color(0xFFF59E0B)
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (uiState.messengerRelayServer == "cloudflare")
                                                "بستر ابری: کلودفلر (فعال)"
                                            else
                                                "بستر ابری: رله پشتیبان (فعال)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row {
                                IconButton(
                                    onClick = { viewModel.syncMessenger() }
                                ) {
                                    if (uiState.isSyncingMessenger) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "بروزرسانی پیام‌ها",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // User Profile Identity Card
                        MessengerProfileBar(
                            myId = if (uiState.messengerCustomUsername.isNotBlank())
                                "@${uiState.messengerCustomUsername}"
                            else
                                uiState.messengerUserId,
                            displayName = uiState.messengerDisplayName,
                            onCopyId = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Samad ID", uiState.messengerUserId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "شناسه کپی شد: ${uiState.messengerUserId}", Toast.LENGTH_SHORT).show()
                            },
                            onEditProfile = { showProfileDialog = true },
                            onSmartPaste = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    viewModel.importFromMessengerText(text)
                                } else {
                                    Toast.makeText(context, "کلیپ‌بورد خالی است", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tab Row (همه, دریافتی, ارسالی)
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "همه گفتگوها (${uiState.messengerMessages.size})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    val count = uiState.messengerMessages.count { !it.isOutgoing }
                                    Text(
                                        text = "ژتون‌های دریافتی ($count)",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = {
                                    val count = uiState.messengerMessages.count { it.isOutgoing }
                                    Text(
                                        text = "ارسالی ($count)",
                                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showSendDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ارسال ژتون / پیام",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (filteredMessages.isEmpty()) {
                        EmptyMessengerState(
                            selectedTab = selectedTab,
                            onSendClicked = { showSendDialog = true },
                            onSmartPaste = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    viewModel.importFromMessengerText(text)
                                } else {
                                    Toast.makeText(context, "کلیپ‌بورد خالی است", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredMessages, key = { it.id }) { message ->
                                ChatMessageCard(
                                    message = message,
                                    viewModel = viewModel,
                                    onShowFullBarcode = { code, title ->
                                        fullBarcodeForgotCode = code
                                        fullBarcodeTitle = title
                                    },
                                    onDelete = { viewModel.deleteChatMessage(message.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fullscreen barcode dialog for instant turnstile scanning
        fullBarcodeForgotCode?.let { code ->
            FullscreenBarcodeDialog(
                forgotCode = code,
                title = fullBarcodeTitle,
                onDismiss = { fullBarcodeForgotCode = null }
            )
        }

        // Send Token Dialog
        if (showSendDialog) {
            SendTokenDialog(
                viewModel = viewModel,
                uiState = uiState,
                onDismiss = { showSendDialog = false }
            )
        }

        // Edit Profile Dialog
        if (showProfileDialog) {
            EditMessengerProfileDialog(
                currentDisplayName = uiState.messengerDisplayName,
                currentUsername = uiState.messengerCustomUsername,
                myId = uiState.messengerUserId,
                onSave = { name, username ->
                    viewModel.updateMessengerProfile(name, username)
                    showProfileDialog = false
                },
                onDismiss = { showProfileDialog = false }
            )
        }
    }
}

@Composable
fun MessengerProfileBar(
    myId: String,
    displayName: String,
    onCopyId: () -> Unit,
    onEditProfile: () -> Unit,
    onSmartPaste: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "شناسه من: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = myId,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onCopyId,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "کپی شناسه من",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onSmartPaste,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "دریافت از کلیپ‌بورد",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش مشخصات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageCard(
    message: ChatMessageEntity,
    viewModel: MainViewModel,
    onShowFullBarcode: (code: String, title: String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val packet: TokenPacket? = remember(message.tokenPacketJson) {
        viewModel.messengerRepository.parsePacketJson(message.tokenPacketJson)
    }

    val isToken = message.packetType != "TEXT_ONLY" && packet != null

    val cardBg = if (message.isOutgoing) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }

    val borderColor = if (isToken) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        Color.Transparent
    }

    val formattedTime = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Sender/Receiver info & time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (message.isOutgoing) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (message.isOutgoing) Icons.Default.Send else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (message.isOutgoing) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (message.isOutgoing) "ارسال به: ${message.receiverId}"
                            else "فرستنده: ${message.senderName} (${message.senderId})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedTime.toPersianDigits(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isToken) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ژتون غذا",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف پیام",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Message text
            if (message.messageText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message.messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // If token packet, render meal token cards with direct barcode
            if (isToken && packet != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = packet.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                for (item in packet.items) {
                    TokenMealItemCard(
                        item = item,
                        senderName = message.senderName,
                        isOutgoing = message.isOutgoing,
                        isClaimed = message.isClaimed,
                        onClaim = {
                            viewModel.claimMessengerToken(message.id, item, message.senderName)
                        },
                        onShowBarcode = { code, title ->
                            onShowFullBarcode(code, title)
                        },
                        onCopyForgotCode = { code ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Forgot Code", code))
                            Toast.makeText(context, "کد ۹ رقمی کپی شد: $code", Toast.LENGTH_SHORT).show()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Share / Forward Button for the whole packet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            val exportText = viewModel.messengerRepository.exportPacketToShareableText(packet)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Samad Token Packet", exportText))
                            Toast.makeText(context, "کد بسته ژتون کپی شد. می‌توانید در هر پیام‌رسانی ارسال کنید.", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اشتراک‌گذاری در ایتا / تلگرام",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TokenMealItemCard(
    item: SharedTokenItem,
    senderName: String,
    isOutgoing: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit,
    onShowBarcode: (code: String, title: String) -> Unit,
    onCopyForgotCode: (code: String) -> Unit
) {
    val barcodeBitmap = remember(item.forgotCardCode) {
        if (item.forgotCardCode.isNotBlank()) {
            QrCodeGenerator.generateBarcodeBitmap(item.forgotCardCode, width = 640, height = 180)
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Food title and meal badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.mealName.ifBlank { "وعده غذا" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.foodName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${item.dayName} (${item.dateJStr})".toPersianDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.besideFoodNames.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "مخلفات: ${item.besideFoodNames}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.selfName.isNotBlank()) {
                Text(
                    text = "محل توزیع: ${item.selfName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Direct Barcode Rendering
            if (item.forgotCardCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable {
                            onShowBarcode(item.forgotCardCode, "${item.mealName} - ${item.foodName}")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (barcodeBitmap != null) {
                            Image(
                                bitmap = barcodeBitmap.asImageBitmap(),
                                contentDescription = "بارکد سلف",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "کد ۹ رقمی: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = item.forgotCardCode.toPersianDigits(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(لمس برای بزرگنمایی و اسکن)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Action buttons: Claim & Copy Code
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isOutgoing) {
                    Button(
                        onClick = onClaim,
                        enabled = !isClaimed,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isClaimed) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isClaimed) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isClaimed) "به رزروهای من اضافه شد" else "افزودن به رزروهای من",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (item.forgotCardCode.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onCopyForgotCode(item.forgotCardCode) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "کپی کد",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FullscreenBarcodeDialog(
    forgotCode: String,
    title: String,
    onDismiss: () -> Unit
) {
    val barcodeBitmap: Bitmap? = remember(forgotCode) {
        QrCodeGenerator.generateBarcodeBitmap(forgotCode, width = 800, height = 260)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title.ifBlank { "بارکد سلف دانشجویی" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "صفحه را روبروی بارکدخوان سلف قرار دهید",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        barcodeBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "بارکد بزرگ سلف",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = forgotCode.toPersianDigits(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("بستن صفحه")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendTokenDialog(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onDismiss: () -> Unit
) {
    var recipientId by remember { mutableStateOf("") }
    var messageNote by remember { mutableStateOf("") }
    var selectedPackageType by remember { mutableIntStateOf(0) } // 0: وعده خاص, 1: یک روز, 2: کل هفته, 3: کل رزروها, 4: فقط متن

    val activeReserves = remember(uiState.allReserves) {
        uiState.allReserves.filter { !it.consumed }
    }

    var selectedReserveIndex by remember { mutableIntStateOf(0) }
    var reserveDropdownExpanded by remember { mutableStateOf(false) }

    var selectedDateStr by remember {
        mutableStateOf(uiState.availableDates.firstOrNull() ?: "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "ارسال ژتون و پیام به دوست",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Recipient ID Field
                OutlinedTextField(
                    value = recipientId,
                    onValueChange = { recipientId = it },
                    label = { Text("شناسه یا نام کاربری دوست (مثال: SMD-84920 یا @ali)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // What to send type chips
                Text(
                    text = "نوع محتوای ارسالی:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeChip(
                        title = "یک وعده",
                        isSelected = selectedPackageType == 0,
                        onClick = { selectedPackageType = 0 }
                    )
                    TypeChip(
                        title = "یک روز",
                        isSelected = selectedPackageType == 1,
                        onClick = { selectedPackageType = 1 }
                    )
                    TypeChip(
                        title = "کل هفته",
                        isSelected = selectedPackageType == 2,
                        onClick = { selectedPackageType = 2 }
                    )
                    TypeChip(
                        title = "کل رزروها",
                        isSelected = selectedPackageType == 3,
                        onClick = { selectedPackageType = 3 }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Options depending on package type
                when (selectedPackageType) {
                    0 -> {
                        // Single meal selection
                        if (activeReserves.isEmpty()) {
                            Text(
                                text = "هیچ رزرو فعالی در برنامه یافت نشد",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            val currentChosen = activeReserves.getOrNull(selectedReserveIndex)
                            ExposedDropdownMenuBox(
                                expanded = reserveDropdownExpanded,
                                onExpandedChange = { reserveDropdownExpanded = !reserveDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = currentChosen?.let { "${it.dayName} (${it.dateJStr}) - ${it.mealName}: ${it.foodName}" } ?: "انتخاب ژتون",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("انتخاب ژتون وعده") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reserveDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = reserveDropdownExpanded,
                                    onDismissRequest = { reserveDropdownExpanded = false }
                                ) {
                                    activeReserves.forEachIndexed { index, reserve ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${reserve.dayName} (${reserve.dateJStr}) - ${reserve.mealName}: ${reserve.foodName}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            },
                                            onClick = {
                                                selectedReserveIndex = index
                                                reserveDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Day selection
                        Text(
                            text = "انتخاب تاریخ روز برای ارسال تمام وعده‌ها:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.availableDates.take(4).forEach { date ->
                                val count = uiState.allReserves.count { it.date == date }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedDateStr == date) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { selectedDateStr = date }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${date.takeLast(5)} ($count)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selectedDateStr == date) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedDateStr == date) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "تمامی رزروهای هفته جاری برای دوست شما فرستاده می‌شود و بارکد هر یک به صورت مجزا در مقصد تولید خواهد شد.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    3 -> {
                        Text(
                            text = "تمام رزروهای فعال شما (${activeReserves.size} ژتون) به صورت یک بسته کامل فرستاده می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Message Note
                OutlinedTextField(
                    value = messageNote,
                    onValueChange = { messageNote = it },
                    label = { Text("متن پیام دلخواه (اختیاری)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (recipientId.isBlank()) return@Button
                            when (selectedPackageType) {
                                0 -> {
                                    val reserve = activeReserves.getOrNull(selectedReserveIndex)
                                    if (reserve != null) {
                                        viewModel.sendSingleReserveViaMessenger(recipientId, reserve, messageNote)
                                    }
                                }
                                1 -> {
                                    if (selectedDateStr.isNotBlank()) {
                                        viewModel.sendDayReservesViaMessenger(recipientId, selectedDateStr, messageNote)
                                    }
                                }
                                2 -> {
                                    viewModel.sendWeekReservesViaMessenger(recipientId, activeReserves, "رزروهای کل هفته", messageNote)
                                }
                                3 -> {
                                    viewModel.sendAllReservesViaMessenger(recipientId, messageNote)
                                }
                            }
                            onDismiss()
                        },
                        enabled = recipientId.isNotBlank() && !uiState.isSendingMessage,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSendingMessage) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Text("ارسال مستقیم")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EditMessengerProfileDialog(
    currentDisplayName: String,
    currentUsername: String,
    myId: String,
    onSave: (displayName: String, username: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentDisplayName) }
    var username by remember { mutableStateOf(currentUsername) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "مشخصات کاربری پیام‌رسان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "شناسه سیستمی یکتا: $myId",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام نمایشی (مثال: علی رضایی)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.removePrefix("@") },
                    label = { Text("نام کاربری دلخواه یکتا (اختیاری، بدون @)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, username) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ذخیره")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMessengerState(
    selectedTab: Int,
    onSendClicked: () -> Unit,
    onSmartPaste: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (selectedTab) {
                1 -> "هنوز ژتونی از دوستان دریافت نکرده‌اید"
                2 -> "هنوز ژتونی برای کسی ارسال نکرده‌اید"
                else -> "پیام‌رسان ژتون‌های سلف"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "شما می‌توانید ژتون یک وعده خاص، کل روز یا هفته را مستقیماً به دوستان خود بفرستید تا بارکد اسکن سلف فوراً در گوشی آن‌ها آماده شود.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSendClicked,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("ارسال ژتون جدید")
            }

            OutlinedButton(
                onClick = onSmartPaste,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("دریافت از کلیپ‌بورد")
            }
        }
    }
}
