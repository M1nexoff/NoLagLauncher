// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeGrid
import uz.m1nex.nolaglauncher.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val grid: StateFlow<GridConfig> = settingsRepository.observeGrid()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeGrid.DEFAULT
        )

    fun setGrid(columns: Int, rows: Int) {
        viewModelScope.launch { settingsRepository.setGrid(columns, rows) }
    }
}
