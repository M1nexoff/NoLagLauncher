package uz.m1nex.nolaglauncher.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.data.local.entity.AppEntity

@Dao
interface AppDao {

    @Query("SELECT * FROM apps ORDER BY page, position")
    fun observeHome(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps")
    suspend fun getAll(): List<AppEntity>

    @Upsert
    suspend fun upsertAll(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE componentKey IN (:keys)")
    suspend fun deleteByKeys(keys: List<String>)

    @Query("UPDATE apps SET page = :page, position = :position WHERE componentKey = :componentKey")
    suspend fun updateLayout(componentKey: String, page: Int, position: Int)
}
