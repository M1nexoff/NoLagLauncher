// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.os.Process
import android.os.UserHandle
import androidx.room.withTransaction
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
import uz.m1nex.nolaglauncher.data.local.LauncherDatabase
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
    private val database: LauncherDatabase,
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

    /**
     * Reconciles the stored layout with the installed apps without disturbing the user's arrangement:
     * existing apps keep their exact (page, position) so deliberate gaps survive, brand-new apps are
     * appended after the last occupied slot, and uninstalled apps are deleted (leaving a gap).
     *
     * @author A'zamxo'ja Iskandarxo'jayev
     */
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

            // One transaction so a package-change sync cannot interleave with a drag edit and write
            // back stale coordinates over a just-made layout change.
            database.withTransaction {
                val existing = appDao.getAll()
                val existingByKey = existing.associateBy { it.componentKey }
                val installedExisting = existing.filter { systemByKey.containsKey(it.componentKey) }

                val occupied = installedExisting
                    .filter { !it.favourite }
                    .mapTo(HashSet()) { it.page * perPage + it.position }
                var nextSlot = (occupied.maxOrNull() ?: -1) + 1

                val rows = ArrayList<AppEntity>(systemByKey.size)
                for (entity in installedExisting) {
                    rows.add(entityOf(entity.componentKey, entity.favourite, entity.favouritePosition, entity.page, entity.position))
                }
                systemByKey.keys
                    .filter { it !in existingByKey }
                    .sortedBy { labels.getValue(it).lowercase() }
                    .forEach { key ->
                        val slot = nextSlot++
                        rows.add(entityOf(key, favourite = false, favouritePosition = -1, page = slot / perPage, position = slot % perPage))
                    }

                val removedKeys = (existingByKey.keys - systemByKey.keys).toList()
                appDao.upsertAll(rows)
                if (removedKeys.isNotEmpty()) {
                    appDao.deleteByKeys(removedKeys)
                    iconDao.deleteByKeys(removedKeys)
                }
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

    /**
     * Places an app on an exact home cell, leaving its old cell empty. If the target cell already
     * holds a home app they swap; if the moving app came from the dock and the target is occupied it
     * is dropped onto the first free slot instead so the occupant is never lost.
     *
     * @author A'zamxo'ja Iskandarxo'jayev
     */
    override suspend fun moveAppToCell(componentKey: String, page: Int, position: Int) {
        val perPage = settingsRepository.getGrid().perPage
        database.withTransaction {
            val all = appDao.getAll()
            val app = all.firstOrNull { it.componentKey == componentKey } ?: return@withTransaction
            val wasFavourite = app.favourite
            val occupant = all.firstOrNull {
                !it.favourite && it.page == page && it.position == position && it.componentKey != componentKey
            }
            when {
                occupant == null -> {
                    appDao.setFavourite(componentKey, favourite = false, favouritePosition = -1)
                    appDao.updateLayout(componentKey, page, position)
                }
                !wasFavourite -> {
                    appDao.updateLayout(occupant.componentKey, app.page, app.position)
                    appDao.updateLayout(componentKey, page, position)
                }
                else -> {
                    val (freePage, freePosition) = firstFreeHomeSlot(all.filter { !it.favourite }, perPage)
                    appDao.setFavourite(componentKey, favourite = false, favouritePosition = -1)
                    appDao.updateLayout(componentKey, freePage, freePosition)
                }
            }
            if (wasFavourite) repackFavourites()
        }
    }

    /**
     * Inserts an app into the dock at [index], shifting the rest aside. Reordering an existing
     * favourite is the same operation (it is removed from its old index first). The app's former home
     * cell is intentionally left empty.
     *
     * @author A'zamxo'ja Iskandarxo'jayev
     */
    override suspend fun addToFavouriteAt(componentKey: String, index: Int): Boolean =
        database.withTransaction {
            val all = appDao.getAll()
            if (all.none { it.componentKey == componentKey }) return@withTransaction false
            val favourites = all.filter { it.favourite }.sortedBy { it.favouritePosition }
            val alreadyFavourite = favourites.any { it.componentKey == componentKey }
            if (!alreadyFavourite && favourites.size >= HomeGrid.MAX_FAVOURITES) return@withTransaction false

            val order = favourites.mapTo(ArrayList()) { it.componentKey }
            order.remove(componentKey)
            order.add(index.coerceIn(0, order.size), componentKey)
            order.forEachIndexed { position, key -> appDao.setFavourite(key, favourite = true, favouritePosition = position) }
            true
        }

    override suspend fun removeFromFavourite(componentKey: String) {
        val perPage = settingsRepository.getGrid().perPage
        database.withTransaction {
            val homeApps = appDao.getAll().filter { !it.favourite }
            val (freePage, freePosition) = firstFreeHomeSlot(homeApps, perPage)
            appDao.setFavourite(componentKey, favourite = false, favouritePosition = -1)
            appDao.updateLayout(componentKey, freePage, freePosition)
            repackFavourites()
        }
    }

    private fun firstFreeHomeSlot(homeApps: List<AppEntity>, perPage: Int): Pair<Int, Int> {
        val occupied = homeApps.mapTo(HashSet()) { it.page * perPage + it.position }
        var slot = 0
        while (slot in occupied) slot++
        return slot / perPage to slot % perPage
    }

    private suspend fun repackFavourites() {
        appDao.getAll()
            .filter { it.favourite }
            .sortedBy { it.favouritePosition }
            .forEachIndexed { index, entity ->
                appDao.setFavourite(entity.componentKey, favourite = true, favouritePosition = index)
            }
    }

    private suspend fun reflowHome(perPage: Int) {
        appDao.getAll()
            .filter { !it.favourite }
            .sortedWith(compareBy({ it.page }, { it.position }))
            .forEachIndexed { index, entity ->
                appDao.updateLayout(entity.componentKey, index / perPage, index % perPage)
            }
    }

    private fun observeGridChanges() {
        scope.launch {
            settingsRepository.observeGrid()
                .map { it.perPage }
                .distinctUntilChanged()
                .drop(1)
                .collect { perPage ->
                    syncMutex.withLock { database.withTransaction { reflowHome(perPage) } }
                }
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
