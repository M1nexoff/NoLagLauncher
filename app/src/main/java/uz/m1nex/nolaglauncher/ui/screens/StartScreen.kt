// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import uz.m1nex.nolaglauncher.ui.widgets.AppButton
import uz.m1nex.nolaglauncher.utils.openHomeSettings

@Composable
fun StartScreen(){
    val context = LocalContext.current
    Scaffold { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Spacer(Modifier.height(16.dp))
            AppButton("Open Home Settings") {
                context.openHomeSettings()
            }
        }
    }
}