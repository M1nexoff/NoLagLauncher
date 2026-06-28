// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow

object LauncherTokens {
    val OnWallpaper = Color.White
    val OnWallpaperMuted = Color.White.copy(alpha = 0.72f)

    val DockBackground = Color.White.copy(alpha = 0.14f)
    val WidgetSurface = Color.White.copy(alpha = 0.12f)
    val CellHighlight = Color.White.copy(alpha = 0.16f)

    val DotSelected = Color.White
    val DotUnselected = Color.White.copy(alpha = 0.4f)

    val LabelShadow = Shadow(
        color = Color.Black.copy(alpha = 0.55f),
        offset = Offset(0f, 1f),
        blurRadius = 6f
    )
}
