// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.Intent
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import uz.m1nex.nolaglauncher.MainActivity
import uz.m1nex.nolaglauncher.ui.theme.LauncherTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ClockText(val time: String, val date: String)

@Composable
fun WidgetPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showWallpaperSoon by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.2f))
            ClockWidget()
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetPill(text = "Settings") {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
                WidgetPill(text = "Wallpaper") { showWallpaperSoon = true }
            }
        }

        AnimatedVisibility(
            visible = showWallpaperSoon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
        ) {
            ComingSoonPill(text = "Wallpapers — coming soon")
        }
    }

    LaunchedEffect(showWallpaperSoon) {
        if (showWallpaperSoon) {
            delay(1800)
            showWallpaperSoon = false
        }
    }
}

@Composable
private fun ClockWidget() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val timeFormat = remember { DateFormat.getTimeFormat(context) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

    val clock by produceState(initialValue = formatClock(timeFormat, dateFormat)) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = formatClock(timeFormat, dateFormat)
                val now = System.currentTimeMillis()
                delay(60_000L - (now % 60_000L) + 50L)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = clock.time,
            style = MaterialTheme.typography.displayLarge.copy(
                color = LauncherTokens.OnWallpaper,
                shadow = LauncherTokens.LabelShadow,
                fontWeight = FontWeight.Light
            )
        )
        Text(
            text = clock.date,
            style = MaterialTheme.typography.titleMedium.copy(
                color = LauncherTokens.OnWallpaperMuted,
                shadow = LauncherTokens.LabelShadow
            )
        )
    }
}

@Composable
private fun WidgetPill(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = LauncherTokens.WidgetSurface,
        contentColor = LauncherTokens.OnWallpaper
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun ComingSoonPill(text: String) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = Color.Black.copy(alpha = 0.55f),
        contentColor = Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatClock(timeFormat: java.text.DateFormat, dateFormat: java.text.DateFormat): ClockText {
    val now = Date()
    return ClockText(time = timeFormat.format(now), date = dateFormat.format(now))
}
