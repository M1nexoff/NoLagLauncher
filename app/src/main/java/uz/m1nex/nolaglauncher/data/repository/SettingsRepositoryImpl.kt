// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.m1nex.nolaglauncher.data.local.dao.SettingsDao
import uz.m1nex.nolaglauncher.data.local.entity.GridSettingsEntity
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeGrid
import uz.m1nex.nolaglauncher.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {

    override fun observeGrid(): Flow<GridConfig> =
        settingsDao.observe().map { it?.toConfig() ?: HomeGrid.DEFAULT }

    override suspend fun getGrid(): GridConfig =
        settingsDao.get()?.toConfig() ?: HomeGrid.DEFAULT

    override suspend fun setGrid(columns: Int, rows: Int) {
        settingsDao.upsert(
            GridSettingsEntity(
                id = 0,
                columns = columns.coerceIn(HomeGrid.MIN, HomeGrid.MAX),
                rows = rows.coerceIn(HomeGrid.MIN, HomeGrid.MAX)
            )
        )
    }

    private fun GridSettingsEntity.toConfig() = GridConfig(columns = columns, rows = rows)
}
