// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <devasgardia@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens.launcher

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import uz.m1nex.nolaglauncher.domain.model.GridConfig
import uz.m1nex.nolaglauncher.domain.model.HomeApp
import uz.m1nex.nolaglauncher.domain.model.HomeGrid
import uz.m1nex.nolaglauncher.ui.theme.LauncherTokens

private val GhostIconSize = 64.dp
private val DockSlotWidth = 72.dp

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color.Transparent) { innerPadding ->
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
    val pageCount = 1 + state.pages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    val onLaunch: (ComponentName) -> Unit = { onIntent(LauncherContract.Intent.LaunchApp(it)) }
    val onDrop: (DraggedItem, Offset) -> Unit = { item, position ->
        val dock = dragState.dockBounds
        when {
            dock != null && dock.contains(position) -> {
                val displayCount = state.favourites.size - if (item.fromFavourite) 1 else 0
                val index = dockInsertIndex(position.x, dock, displayCount)
                onIntent(LauncherContract.Intent.AddToFavouriteAt(item.app.componentKey, index))
            }
            else -> {
                val dataPage = pagerState.currentPage - 1
                if (dataPage >= 0) {
                    val pageRect = dragState.pageBounds[dataPage]
                    when {
                        pageRect != null && pageRect.contains(position) -> {
                            val cell = cellIndexIn(position, pageRect, state.grid)
                            onIntent(LauncherContract.Intent.MoveAppToCell(item.app.componentKey, dataPage, cell))
                        }
                        item.fromFavourite ->
                            onIntent(LauncherContract.Intent.RemoveFromFavourite(item.app.componentKey))
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                beyondViewportPageCount = 1
            ) { pagerIndex ->
                if (pagerIndex == 0) {
                    WidgetPage(modifier = Modifier.fillMaxSize())
                } else {
                    val dataPage = pagerIndex - 1
                    AppPage(
                        dataPage = dataPage,
                        apps = state.pages[dataPage],
                        grid = state.grid,
                        loadIcon = loadIcon,
                        onLaunch = onLaunch,
                        dragState = dragState,
                        onDrop = onDrop
                    )
                }
            }

            PagerDots(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                onDotClick = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )

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
    dataPage: Int,
    apps: List<HomeApp>,
    grid: GridConfig,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, Offset) -> Unit
) {
    val byPosition = remember(apps) { apps.associateBy { it.position } }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    val hoveredCell: State<Int?> = remember(grid) {
        derivedStateOf {
            if (dragState.dragged == null) null
            else bounds?.let { hoveredCellIn(dragState.position, it, grid) }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .onGloballyPositioned {
                val rect = it.boundsInRoot()
                bounds = rect
                dragState.pageBounds[dataPage] = rect
            }
    ) {
        val cellWidth = maxWidth / grid.columns
        val cellHeight = maxHeight / grid.rows

        HoverHighlight(hoveredCell = hoveredCell, grid = grid, cellWidth = cellWidth, cellHeight = cellHeight)

        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until grid.rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (column in 0 until grid.columns) {
                        val index = row * grid.columns + column
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            byPosition[index]?.let { app ->
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
}

@Composable
private fun HoverHighlight(
    hoveredCell: State<Int?>,
    grid: GridConfig,
    cellWidth: Dp,
    cellHeight: Dp
) {
    val index = hoveredCell.value ?: return
    val row = index / grid.columns
    val column = index % grid.columns
    Box(
        modifier = Modifier
            .offset(x = cellWidth * column, y = cellHeight * row)
            .size(width = cellWidth, height = cellHeight)
            .padding(6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(LauncherTokens.CellHighlight)
    )
}

private sealed interface DockEntry {
    val key: Any
}

private data class DockApp(val app: HomeApp) : DockEntry {
    override val key: Any get() = app.componentKey
}

private data object DockGap : DockEntry {
    override val key: Any get() = "::dock-gap::"
}

@Composable
private fun FavouriteDock(
    favourites: List<HomeApp>,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, Offset) -> Unit
) {
    val dragged = dragState.dragged
    val draggedFavouriteKey = if (dragged?.fromFavourite == true) dragged.app.componentKey else null
    val draggingFavourite = draggedFavouriteKey != null
    // Excludes the dragged favourite for placement math, but the dragged cell is never removed from
    // composition below (doing so would kill the gesture that owns the drag).
    val others = remember(favourites, draggedFavouriteKey) {
        if (draggingFavourite) favourites.filter { it.componentKey != draggedFavouriteKey } else favourites
    }
    val dockFull = !draggingFavourite && favourites.size >= HomeGrid.MAX_FAVOURITES
    val insertIndex = remember(others, dockFull) {
        derivedStateOf {
            if (dragState.dragged == null || dockFull) return@derivedStateOf null
            val dock = dragState.dockBounds ?: return@derivedStateOf null
            if (!dock.contains(dragState.position)) null
            else dockInsertIndex(dragState.position.x, dock, others.size)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(LauncherTokens.DockBackground)
            .dockDropTarget(dragState)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val gap = insertIndex.value
        val entries = buildList<DockEntry> {
            if (draggingFavourite) {
                val draggedApp = dragged!!.app
                val originalIndex = favourites.indexOfFirst { it.componentKey == draggedFavouriteKey }
                val insertAt = (gap ?: originalIndex).coerceIn(0, others.size)
                others.forEachIndexed { index, app ->
                    if (index == insertAt) add(DockApp(draggedApp))
                    add(DockApp(app))
                }
                if (insertAt >= others.size) add(DockApp(draggedApp))
            } else {
                others.forEachIndexed { index, app ->
                    if (gap == index) add(DockGap)
                    add(DockApp(app))
                }
                if (gap != null && gap >= others.size) add(DockGap)
            }
        }
        items(entries, key = { it.key }) { entry ->
            when (entry) {
                is DockGap -> Box(
                    modifier = Modifier
                        .width(DockSlotWidth)
                        .fillMaxHeight()
                        .animateItem()
                )

                is DockApp -> Box(
                    modifier = Modifier
                        .width(DockSlotWidth)
                        .fillMaxHeight()
                        .animateItem()
                ) {
                    AppCell(
                        app = entry.app,
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
}

@Composable
private fun AppCell(
    app: HomeApp,
    fromFavourite: Boolean,
    loadIcon: suspend (HomeApp) -> ImageBitmap?,
    onLaunch: (ComponentName) -> Unit,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, Offset) -> Unit
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
            style = MaterialTheme.typography.labelSmall.copy(
                color = LauncherTokens.OnWallpaper,
                shadow = LauncherTokens.LabelShadow
            )
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
    onDotClick: (Int) -> Unit,
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDotClick(index) }
                    .padding(horizontal = 7.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (selected) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (selected) LauncherTokens.DotSelected else LauncherTokens.DotUnselected)
                )
            }
        }
    }
}
