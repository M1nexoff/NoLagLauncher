package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.model.HomeGrid

private val GhostIconSize = 64.dp

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
                is LauncherContract.State.Ready -> ReadyContent(
                    state = current,
                    loadIcon = viewModel::loadIcon,
                    onIntent = viewModel::onIntent
                )
            }
        }
    }
}

@Composable
private fun ReadyContent(
    state: LauncherContract.State.Ready,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onIntent: (LauncherContract.Intent) -> Unit
) {
    val dragState = rememberLauncherDragState()
    val onDrop: (DraggedItem, androidx.compose.ui.geometry.Offset) -> Unit = { item, position ->
        val dock = dragState.dockBounds
        val droppedInDock = dock != null && dock.contains(position)
        when {
            !item.fromFavourite && droppedInDock ->
                onIntent(LauncherContract.Intent.AddToFavourite(item.app.componentKey))
            item.fromFavourite && !droppedInDock ->
                onIntent(LauncherContract.Intent.RemoveFromFavourite(item.app.componentKey))
        }
    }
    val onLaunch: (ComponentName) -> Unit = { onIntent(LauncherContract.Intent.LaunchApp(it)) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (state.pages.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { state.pages.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    beyondViewportPageCount = 1
                ) { pageIndex ->
                    AppPage(
                        apps = state.pages[pageIndex],
                        grid = state.grid,
                        loadIcon = loadIcon,
                        onLaunch = onLaunch,
                        dragState = dragState,
                        onDrop = onDrop
                    )
                }
                PagerDots(
                    pageCount = state.pages.size,
                    currentPage = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            FavouriteDock(
                favourites = state.favourites,
                loadIcon = loadIcon,
                onLaunch = onLaunch,
                dragState = dragState,
                onDrop = onDrop
            )
        }

        DragGhost(dragState = dragState, iconSize = GhostIconSize) { app ->
            AppIcon(app = app, loadIcon = loadIcon, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AppPage(
    apps: List<HomeApp>,
    grid: GridConfig,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, androidx.compose.ui.geometry.Offset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        for (row in 0 until grid.rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until grid.columns) {
                    val index = row * grid.columns + col
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        apps.getOrNull(index)?.let { app ->
                            AppCell(
                                app = app,
                                fromFavourite = false,
                                loadIcon = loadIcon,
                                onLaunch = onLaunch,
                                dragState = dragState,
                                onDrop = onDrop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteDock(
    favourites: List<HomeApp>,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, androidx.compose.ui.geometry.Offset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .dockDropTarget(dragState)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        favourites.forEach { app ->
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
            ) {
                AppCell(
                    app = app,
                    fromFavourite = true,
                    loadIcon = loadIcon,
                    onLaunch = onLaunch,
                    dragState = dragState,
                    onDrop = onDrop
                )
            }
        }
    }
}

@Composable
private fun AppCell(
    app: HomeApp,
    fromFavourite: Boolean,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, androidx.compose.ui.geometry.Offset) -> Unit
) {
    val isDragged = dragState.dragged?.app?.componentKey == app.componentKey
    Column(
        modifier = Modifier
            .fillMaxSize()
            .draggableAppCell(app, fromFavourite, dragState, onDrop)
            .clickable { onLaunch(app.componentName) }
            .padding(4.dp)
            .alpha(if (isDragged) 0.3f else 1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                app = app,
                loadIcon = loadIcon,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            )
        }
        Spacer(Modifier.height(2.dp))
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

@Composable
private fun PagerDots(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
