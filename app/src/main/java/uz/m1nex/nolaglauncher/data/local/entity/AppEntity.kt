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
    val position: Int
)
