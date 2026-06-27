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
