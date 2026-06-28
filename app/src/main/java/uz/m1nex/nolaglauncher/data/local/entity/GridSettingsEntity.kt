// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grid_settings")
data class GridSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val columns: Int,
    val rows: Int
)
