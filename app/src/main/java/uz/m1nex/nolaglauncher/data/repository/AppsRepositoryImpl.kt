package uz.m1nex.nolaglauncher.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Process
import android.os.UserHandle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    private val iconDao: IconDao,
    private val iconCache: IconCache
) : AppsRepository {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val packageManager = context.packageManager
    private val user: UserHandle = Process.myUserHandle()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncMutex = Mutex()

    init {
        registerSystemCallback()
    }

    override fun observeHome(): Flow<List<HomeApp>> =
        appDao.observeHome().map { rows -> rows.map { it.toHomeApp() } }

    override suspend fun syncApps() = syncMutex.withLock {
        withContext(Dispatchers.Default) {
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

            val orderedKeys = ArrayList<String>(systemByKey.size)
            existing.sortedWith(compareBy({ it.page }, { it.position }))
                .forEach { if (systemByKey.containsKey(it.componentKey)) orderedKeys.add(it.componentKey) }
            systemByKey.keys
                .filter { it !in existingKeys }
                .sortedBy { labels.getValue(it).lowercase() }
                .forEach { orderedKeys.add(it) }

            val rows = orderedKeys.mapIndexed { index, key ->
                val component = systemByKey.getValue(key)
                AppEntity(
                    componentKey = key,
                    packageName = component.packageName,
                    className = component.className,
                    label = labels.getValue(key),
                    lastUpdateTime = updateTimes[component.packageName] ?: 0L,
                    page = index / HomeGrid.PER_PAGE,
                    position = index % HomeGrid.PER_PAGE
                )
            }

            val removedKeys = (existingKeys - systemByKey.keys).toList()

            appDao.upsertAll(rows)
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
        orderedKeys.forEachIndexed { index, key ->
            appDao.updateLayout(key, index / HomeGrid.PER_PAGE, index % HomeGrid.PER_PAGE)
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
        position = position
    )
}
