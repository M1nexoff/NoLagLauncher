package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.ComponentName
import uz.m1nex.nolaglauncher.domain.model.HomeApp

class LauncherContract {
    sealed interface State {
        data object Loading : State
        data class Ready(val pages: List<List<HomeApp>>) : State
    }

    sealed interface Intent {
        data class LaunchApp(val componentName: ComponentName) : Intent
    }
}
