// SPDX-FileCopyrightText: 2026 Iskandarxojayev Azamxoja <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import uz.m1nex.nolaglauncher.ui.screens.launcher.LauncherScreen
import uz.m1nex.nolaglauncher.ui.screens.launcher.LauncherViewModel
import uz.m1nex.nolaglauncher.ui.theme.NoLagLauncherTheme

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoLagLauncherTheme {
                LauncherScreen(viewModel)
            }
        }
    }
}
