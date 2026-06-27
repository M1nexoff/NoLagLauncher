package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.model.HomeGrid

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val current = state) {
                is LauncherContract.State.Loading -> CircularProgressIndicator()
                is LauncherContract.State.Ready -> HomePager(
                    pages = current.pages,
                    loadIcon = viewModel::loadIcon,
                    onLaunch = { viewModel.onIntent(LauncherContract.Intent.LaunchApp(it)) }
                )
            }
        }
    }
}

@Composable
private fun HomePager(
    pages: List<List<HomeApp>>,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit
) {
    if (pages.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { pages.size })
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { pageIndex ->
        AppPage(apps = pages[pageIndex], loadIcon = loadIcon, onLaunch = onLaunch)
    }
}

@Composable
private fun AppPage(
    apps: List<HomeApp>,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        for (row in 0 until HomeGrid.ROWS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until HomeGrid.COLUMNS) {
                    val index = row * HomeGrid.COLUMNS + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        apps.getOrNull(index)?.let { app ->
                            AppCell(app = app, loadIcon = loadIcon, onLaunch = onLaunch)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCell(
    app: HomeApp,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onLaunch(app.componentName) }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIcon(app = app, loadIcon = loadIcon, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun AppIcon(
    app: HomeApp,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    modifier: Modifier = Modifier
) {
    val icon by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = app.componentKey,
        key2 = app.lastUpdateTime
    ) {
        value = loadIcon(app)
    }
    val current = icon
    if (current != null) {
        Image(bitmap = current, contentDescription = app.label, modifier = modifier)
    } else {
        Box(modifier)
    }
}
