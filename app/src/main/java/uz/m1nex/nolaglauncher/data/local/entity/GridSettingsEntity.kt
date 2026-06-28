package uz.m1nex.nolaglauncher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grid_settings")
data class GridSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val columns: Int,
    val rows: Int
)
