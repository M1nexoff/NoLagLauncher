// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import uz.m1nex.nolaglauncher.data.local.entity.IconEntity

@Dao
interface IconDao {

    @Query("SELECT * FROM icons WHERE componentKey = :componentKey")
    suspend fun getIcon(componentKey: String): IconEntity?

    @Upsert
    suspend fun upsertIcon(icon: IconEntity)

    @Query("DELETE FROM icons WHERE componentKey IN (:keys)")
    suspend fun deleteByKeys(keys: List<String>)
}
