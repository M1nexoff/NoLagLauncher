package uz.m1nex.nolaglauncher.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import uz.m1nex.nolaglauncher.data.local.dao.AppDao
import uz.m1nex.nolaglauncher.data.local.dao.IconDao
import uz.m1nex.nolaglauncher.data.local.entity.AppEntity
import uz.m1nex.nolaglauncher.data.local.entity.IconEntity

@Database(
    entities = [AppEntity::class, IconEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LauncherDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun iconDao(): IconDao
}
