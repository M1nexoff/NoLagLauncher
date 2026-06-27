package uz.m1nex.nolaglauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uz.m1nex.nolaglauncher.ui.screens.LauncherScreen
import uz.m1nex.nolaglauncher.ui.theme.NoLagLauncherTheme
import uz.m1nex.nolaglauncher.ui.widgets.AppButton
import uz.m1nex.nolaglauncher.utils.openHomeSettings

class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoLagLauncherTheme {
                LauncherScreen()
            }
        }
    }
}