// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.m1nex.nolaglauncher.data.model.AppModel
import androidx.core.net.toUri

suspend fun Context.getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

    return@withContext resolveInfos.map { resolveInfo ->
        val activityInfo = resolveInfo.activityInfo
        AppModel(
            label = resolveInfo.loadLabel(packageManager).toString(),
            packageName = activityInfo.packageName,
            componentName = ComponentName(activityInfo.packageName, activityInfo.name)
        )
    }.sortedBy { it.label.lowercase() }
}

fun Context.getAppIcon(packageName: String): Drawable? {
    return try {
        packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun Context.launchApp(packageName: String) {
    val packageManager = packageManager
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }
}

fun Context.uninstallApp(packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}