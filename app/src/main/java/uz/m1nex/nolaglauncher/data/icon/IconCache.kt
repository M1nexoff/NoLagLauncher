package uz.m1nex.nolaglauncher.data.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.core.graphics.createBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import uz.m1nex.nolaglauncher.data.local.dao.IconDao
import uz.m1nex.nolaglauncher.data.local.entity.IconEntity
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class AppRef(
    val componentName: ComponentName,
    val lastUpdateTime: Long
) {
    val componentKey: String = componentName.flattenToShortString()
}

@Singleton
class IconCache @Inject constructor(
    @ApplicationContext context: Context,
    private val iconDao: IconDao
) {
    private val packageManager = context.packageManager
    private val iconSizePx = (ICON_DP * context.resources.displayMetrics.density).toInt()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(GENERATION_PARALLELISM))

    private val memory: LruCache<String, Bitmap>
    private val inFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()

    init {
        val maxKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        memory = object : LruCache<String, Bitmap>(maxKb / 8) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount / 1024
        }
    }

    suspend fun load(ref: AppRef): Bitmap? {
        val key = memoryKey(ref)
        memory.get(key)?.let { return it }

        val deferred = inFlight.getOrPut(key) {
            scope.async(start = CoroutineStart.LAZY) { loadInternal(ref, key) }
        }
        return try {
            deferred.await()
        } finally {
            inFlight.remove(key, deferred)
        }
    }

    private suspend fun loadInternal(ref: AppRef, key: String): Bitmap? {
        memory.get(key)?.let { return it }

        val cached = iconDao.getIcon(ref.componentKey)
        if (cached != null && cached.generatedForUpdateTime == ref.lastUpdateTime) {
            decodeForDisplay(cached.bytes)?.let {
                memory.put(key, it)
                return it
            }
        }

        val drawable = loadDrawable(ref.componentName) ?: return null
        val raster = rasterize(drawable)
        val bytes = encode(raster)
        iconDao.upsertIcon(IconEntity(ref.componentKey, bytes, ref.lastUpdateTime))

        val display = toDisplayBitmap(raster)
        memory.put(key, display)
        return display
    }

    private fun loadDrawable(component: ComponentName): Drawable? = try {
        packageManager.getActivityIcon(component)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    private fun rasterize(drawable: Drawable): Bitmap {
        val bitmap = createBitmap(iconSizePx, iconSizePx)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, iconSizePx, iconSizePx)
        drawable.draw(canvas)
        return bitmap
    }

    private fun encode(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream(ENCODE_BUFFER_BYTES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return out.toByteArray()
    }

    private fun decodeForDisplay(bytes: ByteArray): Bitmap? {
        val software = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return toDisplayBitmap(software)
    }

    private fun toDisplayBitmap(software: Bitmap): Bitmap {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return software
        val hardware = software.copy(Bitmap.Config.HARDWARE, false) ?: return software
        software.recycle()
        return hardware
    }

    private fun memoryKey(ref: AppRef): String = "${ref.componentKey}#${ref.lastUpdateTime}"

    companion object {
        private const val ICON_DP = 64f
        private const val GENERATION_PARALLELISM = 4
        private const val ENCODE_BUFFER_BYTES = 8 * 1024
    }
}
