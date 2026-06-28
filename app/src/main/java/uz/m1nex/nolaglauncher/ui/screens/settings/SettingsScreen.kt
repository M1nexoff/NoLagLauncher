package uz.m1nex.nolaglauncher.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeGrid
import uz.m1nex.nolaglauncher.ui.widgets.AppButton
import uz.m1nex.nolaglauncher.utils.openHomeSettings

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val grid by viewModel.grid.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var customOpen by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val previewAspect = screenWidth / (screenHeight - 120.dp).coerceAtLeast(240.dp)


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Home grid", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            HomeGrid.PRESETS.forEach { preset ->
                GridOptionCard(
                    title = "${preset.columns} × ${preset.rows}",
                    subtitle = "${preset.perPage} apps per page",
                    selected = grid == preset,
                    onClick = { viewModel.setGrid(preset.columns, preset.rows) }
                )
                Spacer(Modifier.height(8.dp))
            }

            val isCustom = grid !in HomeGrid.PRESETS
            GridOptionCard(
                title = "Custom",
                subtitle = if (isCustom) "${grid.columns} × ${grid.rows}" else "Pick columns and rows",
                selected = isCustom,
                onClick = { customOpen = true }
            )

            Spacer(Modifier.height(24.dp))
            Text(text = "Preview", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            GridPreviewArea(
                grid = grid,
                aspect = previewAspect,
                maxPreviewHeight = 360.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            AppButton(text = "Open Home Settings", modifier = Modifier.fillMaxWidth()) {
                context.openHomeSettings()
            }
        }
    }

    if (customOpen) {
        CustomGridDialog(
            initial = grid,
            previewAspect = previewAspect,
            onConfirm = { columns, rows ->
                viewModel.setGrid(columns, rows)
                customOpen = false
            },
            onDismiss = { customOpen = false }
        )
    }
}

@Composable
private fun GridOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CustomGridDialog(
    initial: GridConfig,
    previewAspect: Float,
    onConfirm: (columns: Int, rows: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var columns by remember { mutableIntStateOf(initial.columns) }
    var rows by remember { mutableIntStateOf(initial.rows) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(columns, rows) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Custom grid") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WheelPicker(
                        label = "Columns",
                        range = HomeGrid.MIN..HomeGrid.MAX,
                        value = initial.columns,
                        onValueChange = { columns = it }
                    )
                    WheelPicker(
                        label = "Rows",
                        range = HomeGrid.MIN..HomeGrid.MAX,
                        value = initial.rows,
                        onValueChange = { rows = it }
                    )
                }
                Spacer(Modifier.height(16.dp))
                GridPreviewArea(
                    grid = GridConfig(columns, rows),
                    aspect = previewAspect,
                    maxPreviewHeight = 220.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
private fun GridPreviewArea(
    grid: GridConfig,
    aspect: Float,
    maxPreviewHeight: Dp,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.heightIn(max = maxPreviewHeight),
        contentAlignment = Alignment.Center
    ) {
        val widthBoundHeight = maxWidth / aspect
        val width: Dp
        val height: Dp
        if (widthBoundHeight <= maxHeight) {
            width = maxWidth
            height = widthBoundHeight
        } else {
            height = maxHeight
            width = maxHeight * aspect
        }
        GridPreview(
            grid = grid,
            modifier = Modifier
                .width(width)
                .height(height)
        )
    }
}

@Composable
private fun GridPreview(
    grid: GridConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0 until grid.rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (col in 0 until grid.columns) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}
