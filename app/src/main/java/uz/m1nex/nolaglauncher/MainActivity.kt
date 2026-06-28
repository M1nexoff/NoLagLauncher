// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uz.m1nex.nolaglauncher.ui.screens.settings.SettingsScreen
import uz.m1nex.nolaglauncher.ui.screens.settings.SettingsViewModel
import uz.m1nex.nolaglauncher.ui.theme.NoLagLauncherTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoLagLauncherTheme {
                SettingsScreen(viewModel)
            }
        }
    }
}
