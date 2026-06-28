package uz.m1nex.nolaglauncher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.m1nex.nolaglauncher.data.local.dao.AppDao
import uz.m1nex.nolaglauncher.data.local.dao.IconDao
import uz.m1nex.nolaglauncher.data.local.dao.SettingsDao
import uz.m1nex.nolaglauncher.data.local.entity.AppEntity
import uz.m1nex.nolaglauncher.data.local.entity.GridSettingsEntity
import uz.m1nex.nolaglauncher.data.local.entity.IconEntity

@Database(
    entities = [AppEntity::class, IconEntity::class, GridSettingsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun iconDao(): IconDao
    abstract fun settingsDao(): SettingsDao
}
