package uz.m1nex.nolaglauncher.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import uz.m1nex.nolaglauncher.utils.getAppIcon
import java.io.File
import androidx.core.graphics.createBitmap
import java.io.FileOutputStream

class AppsRepositoryImpl @Inject constructor(
    private val context: Context
): AppsRepository {
    private val memoryCache: LruCache<String, Bitmap>
    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    private val diskCacheDir = File(context.cacheDir, "app_icons").apply { mkdirs() }
    private val iconSizePx = (56 * context.resources.displayMetrics.density).toInt()

    override suspend fun getIcon(packageName: String): Result<Bitmap> = withContext(Dispatchers.IO){
        memoryCache.get(packageName)?.let { return@withContext Result.success(it) }

        val diskFile = File(diskCacheDir, "$packageName.webp")
        if (diskFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(diskFile.absolutePath)
            if (bitmap != null) {
                memoryCache.put(packageName, bitmap)
                return@withContext Result.success(bitmap)
            }
        }

        val drawable = context.getAppIcon(packageName) ?: return@withContext Result.failure(
            Exception("Can't get app icon"))

        val bitmap = drawableToBitmap(drawable, iconSizePx)

        FileOutputStream(diskFile).use { out ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out)

            }
        }

        memoryCache.put(packageName, bitmap)

        return@withContext Result.success(bitmap)
    }

    private fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            if (drawable.bitmap.width == size && drawable.bitmap.height == size) {
                return drawable.bitmap
            }
        }

        // ARGB_8888 is required to preserve icon transparency
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}