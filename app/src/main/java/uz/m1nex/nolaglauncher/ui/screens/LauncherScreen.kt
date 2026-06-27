package uz.m1nex.nolaglauncher.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import uz.m1nex.nolaglauncher.ui.widgets.AppButton
import uz.m1nex.nolaglauncher.utils.openHomeSettings

@Composable
fun LauncherScreen() {
    val context = LocalContext.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            Spacer(Modifier.fillMaxSize())
            AppButton("Open Home Settings", modifier = Modifier.fillMaxWidth()) {
                context.openHomeSettings()
            }
        }
    }
}