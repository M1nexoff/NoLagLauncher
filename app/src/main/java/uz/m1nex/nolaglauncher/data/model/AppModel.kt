package uz.m1nex.nolaglauncher.data.model

import android.content.ComponentName

data class AppModel(
    val label: String,
    val packageName: String,
    val componentName: ComponentName
)