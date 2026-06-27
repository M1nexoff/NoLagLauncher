package uz.m1nex.nolaglauncher.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.net.toUri

/**
 * @param action use [Settings] actions like [Settings.ACTION_HOME_SETTINGS]
 * @author Iskandarxojayev Azamxoja
 *
 * */
fun Context.openSettings(action: String){
    val intent = Intent(action)
    startActivity(intent)
}

fun Context.openHomeSettings(){
    openSettings(Settings.ACTION_HOME_SETTINGS)
}

fun Context.openAppSettings(packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:$packageName".toUri()
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}