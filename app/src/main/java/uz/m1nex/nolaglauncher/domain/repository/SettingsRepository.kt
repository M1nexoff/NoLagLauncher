// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.domain.model.GridConfig

interface SettingsRepository {
    fun observeGrid(): Flow<GridConfig>
    suspend fun getGrid(): GridConfig
    suspend fun setGrid(columns: Int, rows: Int)
}
