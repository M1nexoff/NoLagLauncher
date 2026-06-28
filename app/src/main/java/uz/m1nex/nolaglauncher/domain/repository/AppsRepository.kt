package uz.m1nex.nolaglauncher.domain.repository

import android.content.ComponentName
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import uz.m1nex.nolaglauncher.domain.model.HomeApp

interface AppsRepository {
    fun observeHome(): Flow<List<HomeApp>>
    suspend fun observeFavourite(): Flow<List<HomeApp>>
    suspend fun syncApps()
    suspend fun loadIcon(app: HomeApp): Bitmap?
    fun launchApp(componentName: ComponentName)
    suspend fun moveAppToCell(componentKey: String, page: Int, position: Int)
    suspend fun addToFavouriteAt(componentKey: String, index: Int): Boolean
    suspend fun removeFromFavourite(componentKey: String)
}
