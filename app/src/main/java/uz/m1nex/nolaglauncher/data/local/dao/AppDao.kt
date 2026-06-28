// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.data.local.entity.AppEntity

@Dao
interface AppDao {

    @Query("SELECT * FROM apps WHERE favourite = 0 ORDER BY page, position")
    fun observeHome(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE favourite = 1 ORDER BY favouritePosition")
    fun observeFavourite(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps")
    suspend fun getAll(): List<AppEntity>

    @Query("SELECT COUNT(*) FROM apps WHERE favourite = 1")
    suspend fun favouriteCount(): Int

    @Upsert
    suspend fun upsertAll(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE componentKey IN (:keys)")
    suspend fun deleteByKeys(keys: List<String>)

    @Query("UPDATE apps SET page = :page, position = :position WHERE componentKey = :componentKey")
    suspend fun updateLayout(componentKey: String, page: Int, position: Int)

    @Query("UPDATE apps SET favourite = :favourite, favouritePosition = :favouritePosition WHERE componentKey = :componentKey")
    suspend fun setFavourite(componentKey: String, favourite: Boolean, favouritePosition: Int)
}
