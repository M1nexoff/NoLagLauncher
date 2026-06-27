package uz.m1nex.nolaglauncher.ui.widgets

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppButton(text: String, modifier: Modifier = Modifier, onClick: ()->Unit){
    Button(onClick,modifier= modifier) {
        Text(text)
    }
}
