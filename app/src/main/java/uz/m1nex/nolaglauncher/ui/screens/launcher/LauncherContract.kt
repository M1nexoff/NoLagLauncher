package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.ComponentName
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeApp

class LauncherContract {
    sealed interface State {
        data object Loading : State
        data class Ready(
            val pages: List<List<HomeApp>>,
            val favourites: List<HomeApp>,
            val grid: GridConfig
        ) : State
    }

    sealed interface Intent {
        data class LaunchApp(val componentName: ComponentName) : Intent
        data class AddToFavourite(val componentKey: String) : Intent
        data class RemoveFromFavourite(val componentKey: String) : Intent
    }
}
