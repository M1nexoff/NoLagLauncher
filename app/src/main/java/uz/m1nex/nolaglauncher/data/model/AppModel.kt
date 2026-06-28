// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.model

import android.content.ComponentName

data class AppModel(
    val label: String,
    val packageName: String,
    val componentName: ComponentName
)