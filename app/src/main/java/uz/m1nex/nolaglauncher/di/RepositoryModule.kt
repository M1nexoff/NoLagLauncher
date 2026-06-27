package uz.m1nex.nolaglauncher.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.m1nex.nolaglauncher.data.repository.AppsRepositoryImpl
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsAppsRepository(repository: AppsRepositoryImpl): AppsRepository

}