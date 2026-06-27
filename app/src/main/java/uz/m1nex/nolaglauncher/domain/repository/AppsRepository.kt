package uz.m1nex.nolaglauncher.domain.repository

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface AppsRepository {
    suspend fun getIcon(packageName: String): Result<Bitmap>
}