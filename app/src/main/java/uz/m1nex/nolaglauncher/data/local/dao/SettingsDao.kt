package uz.m1nex.nolaglauncher.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.data.local.entity.GridSettingsEntity

@Dao
interface SettingsDao {

    @Query("SELECT * FROM grid_settings WHERE id = 0")
    fun observe(): Flow<GridSettingsEntity?>

    @Query("SELECT * FROM grid_settings WHERE id = 0")
    suspend fun get(): GridSettingsEntity?

    @Upsert
    suspend fun upsert(settings: GridSettingsEntity)
}
