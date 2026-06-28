// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val componentKey: String,
    val packageName: String,
    val className: String,
    val label: String,
    val lastUpdateTime: Long,
    val page: Int,
    val position: Int,
    val favourite: Boolean = false,
    val favouritePosition: Int = -1
)
