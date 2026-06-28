package uz.m1nex.nolaglauncher.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Process
import android.os.UserHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.m1nex.nolaglauncher.data.icon.AppRef
import uz.m1nex.nolaglauncher.data.icon.IconCache
import uz.m1nex.nolaglauncher.data.local.dao.AppDao
import uz.m1nex.nolaglauncher.data.local.dao.IconDao
import uz.m1nex.nolaglauncher.data.local.entity.AppEntity
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.model.HomeGrid
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import uz.m1nex.nolaglauncher.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    private val iconDao: IconDao,
    private val iconCache: IconCache,
    private val settingsRepository: SettingsRepository
) : AppsRepository {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val packageManager = context.packageManager
    private val user: UserHandle = Process.myUserHandle()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncMutex = Mutex()

    init {
        registerSystemCallback()
        observeGridChanges()
    }

    override fun observeHome(): Flow<List<HomeApp>> =
        appDao.observeHome().map { rows -> rows.map { it.toHomeApp() } }

    override suspend fun observeFavourite(): Flow<List<HomeApp>> =
        appDao.observeFavourite().map { rows -> rows.map { it.toHomeApp() } }

    override suspend fun syncApps() = syncMutex.withLock {
        withContext(Dispatchers.Default) {
            val perPage = settingsRepository.getGrid().perPage

            val activities = launcherApps.getActivityList(null, user)
            val systemByKey = LinkedHashMap<String, ComponentName>(activities.size)
            val labels = HashMap<String, String>(activities.size)
            for (info in activities) {
                val component = info.componentName
                val key = component.flattenToShortString()
                systemByKey[key] = component
                labels[key] = info.label.toString()
            }

            val updateTimes = packageManager.getInstalledPackages(0)
                .associate { it.packageName to it.lastUpdateTime }

            val existing = appDao.getAll()
            val existingKeys = existing.mapTo(HashSet()) { it.componentKey }

            val favourites = existing
                .filter { it.favourite && systemByKey.containsKey(it.componentKey) }
                .sortedBy { it.favouritePosition }

            val homeOrderedKeys = ArrayList<String>(systemByKey.size)
            existing
                .filter { !it.favourite && systemByKey.containsKey(it.componentKey) }
                .sortedWith(compareBy({ it.page }, { it.position }))
                .forEach { homeOrderedKeys.add(it.componentKey) }
            systemByKey.keys
                .filter { it !in existingKeys }
                .sortedBy { labels.getValue(it).lowercase() }
                .forEach { homeOrderedKeys.add(it) }

            fun entityOf(key: String, favourite: Boolean, favouritePosition: Int, page: Int, position: Int): AppEntity {
                val component = systemByKey.getValue(key)
                return AppEntity(
                    componentKey = key,
                    packageName = component.packageName,
                    className = component.className,
                    label = labels.getValue(key),
                    lastUpdateTime = updateTimes[component.packageName] ?: 0L,
                    page = page,
                    position = position,
                    favourite = favourite,
                    favouritePosition = favouritePosition
                )
            }

            val favRows = favourites.mapIndexed { index, entity ->
                entityOf(entity.componentKey, favourite = true, favouritePosition = index, page = 0, position = 0)
            }
            val homeRows = homeOrderedKeys.mapIndexed { index, key ->
                entityOf(key, favourite = false, favouritePosition = -1, page = index / perPage, position = index % perPage)
            }

            val removedKeys = (existingKeys - systemByKey.keys).toList()

            appDao.upsertAll(favRows + homeRows)
            if (removedKeys.isNotEmpty()) {
                appDao.deleteByKeys(removedKeys)
                iconDao.deleteByKeys(removedKeys)
            }
        }
    }

    override suspend fun loadIcon(app: HomeApp): Bitmap? =
        iconCache.load(AppRef(app.componentName, app.lastUpdateTime))

    override fun launchApp(componentName: ComponentName) {
        try {
            launcherApps.startMainActivity(componentName, user, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun saveOrder(orderedKeys: List<String>) {
        val perPage = settingsRepository.getGrid().perPage
        orderedKeys.forEachIndexed { index, key ->
            appDao.updateLayout(key, index / perPage, index % perPage)
        }
    }

    /**
     * Moves an app into the favourites dock (bottom row). Returns false and does nothing when the
     * dock is already full ([HomeGrid.MAX_FAVOURITES]). The app is appended to the end of the dock
     * and the home pages are re-packed so the gap it left behind is closed.
     *
     * @author Iskandarxojayev Azamxoja
     */
    override suspend fun addToFavourite(componentKey: String): Boolean = syncMutex.withLock {
        val count = appDao.favouriteCount()
        if (count >= HomeGrid.MAX_FAVOURITES) return@withLock false
        appDao.setFavourite(componentKey, favourite = true, favouritePosition = count)
        repackHome()
        true
    }

    /**
     * Moves an app out of the favourites dock back onto the home pages, appended after the last app,
     * then compacts both the home pages and the remaining dock positions.
     *
     * @author Iskandarxojayev Azamxoja
     */
    override suspend fun removeFromFavourite(componentKey: String) = syncMutex.withLock {
        appDao.setFavourite(componentKey, favourite = false, favouritePosition = -1)
        appDao.updateLayout(componentKey, page = Int.MAX_VALUE, position = Int.MAX_VALUE)
        repackHome()
        repackFavourites()
    }

    private suspend fun repackHome() {
        val perPage = settingsRepository.getGrid().perPage
        appDao.getAll()
            .filter { !it.favourite }
            .sortedWith(compareBy({ it.page }, { it.position }))
            .forEachIndexed { index, entity ->
                appDao.updateLayout(entity.componentKey, index / perPage, index % perPage)
            }
    }

    private suspend fun repackFavourites() {
        appDao.getAll()
            .filter { it.favourite }
            .sortedBy { it.favouritePosition }
            .forEachIndexed { index, entity ->
                appDao.setFavourite(entity.componentKey, favourite = true, favouritePosition = index)
            }
    }

    private fun observeGridChanges() {
        scope.launch {
            settingsRepository.observeGrid()
                .map { it.perPage }
                .distinctUntilChanged()
                .drop(1)
                .collect { triggerSync() }
        }
    }

    private fun registerSystemCallback() {
        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) = triggerSync()
            override fun onPackageRemoved(packageName: String, user: UserHandle) = triggerSync()
            override fun onPackageChanged(packageName: String, user: UserHandle) = triggerSync()
            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = triggerSync()
            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = triggerSync()
        })
    }

    private fun triggerSync() {
        scope.launch { syncApps() }
    }

    private fun AppEntity.toHomeApp() = HomeApp(
        componentKey = componentKey,
        packageName = packageName,
        className = className,
        label = label,
        lastUpdateTime = lastUpdateTime,
        page = page,
        position = position,
        favourite = favourite
    )
}
