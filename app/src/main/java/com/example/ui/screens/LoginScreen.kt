package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AmbientOrbsScaffold
import com.example.ui.components.SamadBrandTitle
import com.example.ui.theme.SamadOnSurface
import com.example.ui.theme.SamadOnSurfaceVariant
import com.example.ui.theme.SamadOutline
import com.example.ui.theme.SamadPrimary
import com.example.ui.theme.SamadPrimaryContainer
import com.example.ui.theme.SamadSecondary
import com.example.ui.theme.SamadSurfaceBright
import com.example.ui.theme.SamadSurfaceContainerLow
import com.example.ui.theme.SamadSurfaceContainerLowest
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf(viewModel.prefs.username ?: "") }
    var password by remember { mutableStateOf(viewModel.prefs.password ?: "") }
    var rememberPassword by remember { mutableStateOf(viewModel.prefs.rememberPassword) }
    var passwordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AmbientOrbsScaffold {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Unified, Single Header Branding (no duplicate logos)
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            SamadPrimary.copy(alpha = 0.6f),
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_samad_logo),
                                contentDescription = "لوگوی سماد",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(18.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "سَماد",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SamadPrimary)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "QR",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "دانشگاه فنی و حرفه‌ای",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SamadPrimary
                        )

                        Text(
                            text = "سامانه هوشمند دریافت خودکار ژتون و بارکد سلف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Hero Bento Card for Login
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(22.dp)
                            ) {
                                Text(
                                    text = "ورود به حساب کاربری",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "اطلاعات ورود به سامانه تغذیه سماد خود را وارد نمایید",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Username / Student Number Field
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("شماره دانشجویی یا نام کاربری") },
                                    placeholder = { Text("مثال: 4011234567") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = SamadPrimary
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SamadPrimary,
                                        unfocusedBorderColor = Color(0x2B006B2C),
                                        focusedContainerColor = SamadSurfaceBright,
                                        unfocusedContainerColor = SamadSurfaceBright,
                                        focusedLabelColor = SamadPrimary,
                                        cursorColor = SamadPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("username_input")
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Password Field
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("رمز عبور") },
                                    placeholder = { Text("رمز عبور سامانه سماد") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SamadPrimary
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (passwordVisible) "مخفی کردن" else "نمایش رمز",
                                                tint = SamadOutline
                                            )
                                        }
                                    },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.login(username, password, rememberPassword)
                                        }
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SamadPrimary,
                                        unfocusedBorderColor = Color(0x2B006B2C),
                                        focusedContainerColor = SamadSurfaceBright,
                                        unfocusedContainerColor = SamadSurfaceBright,
                                        focusedLabelColor = SamadPrimary,
                                        cursorColor = SamadPrimary
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Remember Password Checkbox
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { rememberPassword = !rememberPassword }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = rememberPassword,
                                        onCheckedChange = { rememberPassword = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SamadPrimary,
                                            uncheckedColor = SamadOutline
                                        ),
                                        modifier = Modifier.testTag("remember_password_checkbox")
                                    )
                                    Text(
                                        text = "ذخیره مشخصات برای ورودهای بعدی",
                                        fontSize = 12.sp,
                                        color = SamadOnSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Login Button
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.login(username, password, rememberPassword)
                                    },
                                    enabled = !uiState.isLoading,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SamadPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("login_button")
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "در حال ارتباط با سامانه سماد...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    } else {
                                        Text(
                                            text = "ورود به سامانه",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Secondary Bento: Offline & Privacy Tip Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SamadSurfaceContainerLow),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1F006B2C)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SamadPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = SamadPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "این برنامه بدون نیاز به اینترنت در سلف کار می‌کند و کدهای فراموشی به‌صورت خودکار در دستگاه ذخیره و تولید می‌شوند.",
                                    fontSize = 11.sp,
                                    lineHeight = 17.sp,
                                    color = SamadOnSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
