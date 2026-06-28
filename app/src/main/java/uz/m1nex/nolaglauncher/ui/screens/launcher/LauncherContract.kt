// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

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
        data class MoveAppToCell(val componentKey: String, val page: Int, val position: Int) : Intent
        data class AddToFavouriteAt(val componentKey: String, val index: Int) : Intent
        data class RemoveFromFavourite(val componentKey: String) : Intent
    }
}
