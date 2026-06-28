package uz.m1nex.nolaglauncher.ui.screens.launcher

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import uz.m1nex.nolaglauncher.domain.model.HomeApp

data class DraggedItem(val app: HomeApp, val fromFavourite: Boolean)

/**
 * Shared, screen-level state for a single in-progress drag. [dragged] and [position] are Compose
 * state so the floating ghost recomposes/redraws as the finger moves, while [dockBounds] is a plain
 * field read only at drop time (it never needs to trigger recomposition).
 *
 * @author Iskandarxojayev Azamxoja
 */
@Stable
class LauncherDragState {
    var dragged by mutableStateOf<DraggedItem?>(null)
    var position by mutableStateOf(Offset.Zero)
    var dockBounds: Rect? = null
}

@Composable
fun rememberLauncherDragState(): LauncherDragState = remember { LauncherDragState() }

/**
 * Makes a cell draggable after a long press. The pointer position reported by the gesture is local
 * to this cell, so it is added to the cell's own root offset ([positionInRoot]) to produce a
 * window-absolute coordinate that the ghost overlay and the dock hit-test can both reason about.
 *
 * @author Iskandarxojayev Azamxoja
 */
fun Modifier.draggableAppCell(
    app: HomeApp,
    fromFavourite: Boolean,
    dragState: LauncherDragState,
    onDrop: (DraggedItem, Offset) -> Unit
): Modifier = composed {
    var cellRoot by remember { mutableStateOf(Offset.Zero) }
    this
        .onGloballyPositioned { cellRoot = it.positionInRoot() }
        .pointerInput(app.componentKey, fromFavourite) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    dragState.dragged = DraggedItem(app, fromFavourite)
                    dragState.position = cellRoot + offset
                },
                onDrag = { change, _ ->
                    change.consume()
                    dragState.position = cellRoot + change.position
                },
                onDragEnd = {
                    val item = dragState.dragged
                    val dropPosition = dragState.position
                    dragState.dragged = null
                    if (item != null) onDrop(item, dropPosition)
                },
                onDragCancel = { dragState.dragged = null }
            )
        }
}

fun Modifier.dockDropTarget(dragState: LauncherDragState): Modifier =
    this.onGloballyPositioned { dragState.dockBounds = it.boundsInRoot() }

@Composable
fun DragGhost(
    dragState: LauncherDragState,
    iconSize: Dp,
    content: @Composable (HomeApp) -> Unit
) {
    val item = dragState.dragged ?: return
    val half = with(LocalDensity.current) { iconSize.toPx() / 2f }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = dragState.position.x - half
                    translationY = dragState.position.y - half
                    alpha = 0.9f
                }
                .size(iconSize)
        ) {
            content(item.app)
        }
    }
}
