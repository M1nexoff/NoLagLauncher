// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.domain.model

import android.content.ComponentName

data class HomeApp(
    val componentKey: String,
    val packageName: String,
    val className: String,
    val label: String,
    val lastUpdateTime: Long,
    val page: Int,
    val position: Int,
    val favourite: Boolean = false
) {
    val componentName: ComponentName get() = ComponentName(packageName, className)
}
