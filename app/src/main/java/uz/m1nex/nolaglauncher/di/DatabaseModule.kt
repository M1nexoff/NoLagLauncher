// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import uz.m1nex.nolaglauncher.data.local.LauncherDatabase
import uz.m1nex.nolaglauncher.data.local.dao.AppDao
import uz.m1nex.nolaglauncher.data.local.dao.IconDao
import uz.m1nex.nolaglauncher.data.local.dao.SettingsDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LauncherDatabase =
        Room.databaseBuilder(context, LauncherDatabase::class.java, "launcher.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideAppDao(database: LauncherDatabase): AppDao = database.appDao()

    @Provides
    fun provideIconDao(database: LauncherDatabase): IconDao = database.iconDao()

    @Provides
    fun provideSettingsDao(database: LauncherDatabase): SettingsDao = database.settingsDao()
}
