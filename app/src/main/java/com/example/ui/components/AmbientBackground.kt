package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.SamadDarkBackground
import com.example.ui.theme.SamadDarkPrimary
import com.example.ui.theme.SamadDarkPrimaryContainer
import com.example.ui.theme.SamadPrimary

/**
 * Ambient background container with soft mint, emerald, and amber light orbs.
 * Supports both Light and Dark modes seamlessly.
 */
@Composable
fun AmbientOrbsScaffold(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == SamadDarkBackground || isSystemInDarkTheme()

    val topOrbColor = if (isDark) Color(0x33005322) else Color(0x55A7F3D0)
    val midOrbColor = if (isDark) Color(0x241FD66F) else Color(0x66D1FAE5)
    val botOrbColor = if (isDark) Color(0x28633F00) else Color(0x4DFEF3C7)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top-right soft emerald orb
        Box(
            modifier = Modifier
                .offset(x = 60.dp, y = (-50).dp)
                .align(Alignment.TopEnd)
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(topOrbColor, Color.Transparent)
                    )
                )
        )

        // Middle-left soft mint orb
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = 200.dp)
                .align(Alignment.TopStart)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(midOrbColor, Color.Transparent)
                    )
                )
        )

        // Bottom-right warm amber orb
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = (-60).dp)
                .align(Alignment.BottomEnd)
                .size(250.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(botOrbColor, Color.Transparent)
                    )
                )
        )

        // Foreground content
        content()
    }
}

/**
 * Samad Brand Title with superscript square "QR" badge and University caption.
 */
@Composable
fun SamadBrandTitle(
    universityName: String = "دانشگاه فنی و حرفه‌ای",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == SamadDarkBackground
    val universityColor = if (isDark) SamadDarkPrimary else Color(0xFF047857)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = universityName,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = universityColor,
                letterSpacing = 0.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سَماد",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Superscript square QR badge
                Box(
                    modifier = Modifier
                        .offset(y = (-3).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) SamadDarkPrimaryContainer else SamadPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "QR",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Logo Container in rounded-2xl squircle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_samad_logo),
                contentDescription = "لوگوی سماد",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}
