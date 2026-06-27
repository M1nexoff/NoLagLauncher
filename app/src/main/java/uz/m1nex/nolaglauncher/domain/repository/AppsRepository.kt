package uz.m1nex.nolaglauncher.domain.repository

import android.content.ComponentName
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.domain.model.HomeApp

interface AppsRepository {
    fun observeHome(): Flow<List<HomeApp>>
    suspend fun syncApps()
    suspend fun loadIcon(app: HomeApp): Bitmap?
    fun launchApp(componentName: ComponentName)
    suspend fun saveOrder(orderedKeys: List<String>)
}
