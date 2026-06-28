// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.m1nex.nolaglauncher.data.repository.AppsRepositoryImpl
import uz.m1nex.nolaglauncher.data.repository.SettingsRepositoryImpl
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import uz.m1nex.nolaglauncher.domain.repository.SettingsRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsAppsRepository(repository: AppsRepositoryImpl): AppsRepository

    @Binds
    @Singleton
    abstract fun bindsSettingsRepository(repository: SettingsRepositoryImpl): SettingsRepository

}