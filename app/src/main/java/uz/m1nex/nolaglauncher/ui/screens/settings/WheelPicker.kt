// SPDX-FileCopyrightText: 2026 A'zamxo'ja Iskandarxo'jayev <aiskandarxojayev@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package uz.m1nex.nolaglauncher.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val ItemHeight = 44.dp
private const val VisibleItems = 5

/**
 * A snapping number wheel. The list is padded top and bottom by two item heights so the first and
 * last values can rest in the vertical centre; with that padding the centred value is simply
 * [androidx.compose.foundation.lazy.LazyListState.firstVisibleItemIndex], rounded up when the list
 * has scrolled past the half-way point of an item.
 *
 * @author A'zamxo'ja Iskandarxo'jayev
 */
@Composable
fun WheelPicker(
    label: String,
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val values = remember(range) { range.toList() }
    val itemHeightPx = with(LocalDensity.current) { ItemHeight.toPx() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (value - range.first).coerceIn(0, values.lastIndex)
    )

    val centerIndex by remember {
        derivedStateOf {
            val base = listState.firstVisibleItemIndex
            if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2f) base + 1 else base
        }
    }

    LaunchedEffect(centerIndex) {
        onValueChange(range.first + centerIndex.coerceIn(0, values.lastIndex))
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.height(ItemHeight * VisibleItems),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(ItemHeight)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    )
            )
            LazyColumn(
                state = listState,
                flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
                contentPadding = PaddingValues(vertical = ItemHeight * 2),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(values) { index, number ->
                    val selected = index == centerIndex
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(ItemHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            style = if (selected) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.bodyLarge
                            },
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
