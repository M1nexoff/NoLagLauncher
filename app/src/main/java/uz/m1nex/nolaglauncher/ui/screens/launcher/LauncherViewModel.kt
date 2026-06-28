// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens.launcher

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.repository.AppsRepository
import uz.m1nex.nolaglauncher.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val repository: AppsRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val firstSyncDone = MutableStateFlow(false)
    private val favourites = flow { emitAll(repository.observeFavourite()) }

    val state: StateFlow<LauncherContract.State> =
        combine(
            repository.observeHome(),
            favourites,
            settingsRepository.observeGrid(),
            firstSyncDone
        ) { apps, favourites, grid, synced ->
            if (apps.isEmpty() && favourites.isEmpty() && !synced) {
                LauncherContract.State.Loading
            } else {
                LauncherContract.State.Ready(
                    pages = buildPages(apps),
                    favourites = favourites,
                    grid = grid
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LauncherContract.State.Loading
        )

    init {
        viewModelScope.launch {
            repository.syncApps()
            firstSyncDone.value = true
        }
    }

    fun onIntent(intent: LauncherContract.Intent) {
        when (intent) {
            is LauncherContract.Intent.LaunchApp -> repository.launchApp(intent.componentName)
            is LauncherContract.Intent.MoveAppToCell -> viewModelScope.launch {
                repository.moveAppToCell(intent.componentKey, intent.page, intent.position)
            }
            is LauncherContract.Intent.AddToFavouriteAt -> viewModelScope.launch {
                repository.addToFavouriteAt(intent.componentKey, intent.index)
            }
            is LauncherContract.Intent.RemoveFromFavourite -> viewModelScope.launch {
                repository.removeFromFavourite(intent.componentKey)
            }
        }
    }

    suspend fun loadIcon(app: HomeApp): ImageBitmap? = repository.loadIcon(app)?.asImageBitmap()

    private fun buildPages(apps: List<HomeApp>): List<List<HomeApp>> {
        val maxPage = apps.maxOfOrNull { it.page } ?: return emptyList()
        return (0..maxPage).map { page -> apps.filter { it.page == page } }
    }
}
