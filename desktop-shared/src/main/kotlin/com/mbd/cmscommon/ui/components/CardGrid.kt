package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Number of entity cards shown per row in the app's card grids. Desktop shows 4 across; the
 * mobile-shared copy of this file sets 2. Single source of truth so every directory screen
 * (departments, sessions, teachers, students, subjects) stays consistent.
 */
const val CardGridColumns = 4

/**
 * A full-width row inside a [CardGrid] — used for heroes, summaries, search/filter bars, section
 * headers, empty states, and skeletons that should span every column rather than sit in one cell.
 */
fun LazyGridScope.fullSpanItem(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { content() }

/** [count] full-width rows inside a [CardGrid] — e.g. skeleton placeholders while loading. */
fun LazyGridScope.fullSpanItems(count: Int, content: @Composable (Int) -> Unit) =
    items(count, span = { GridItemSpan(maxLineSpan) }) { content(it) }

/**
 * Responsive card grid used by every entity directory. Cells flow [columns] across; wrap any
 * non-card content (headers/filters/empty states) in [fullSpanItem] so it stretches full-width.
 */
@Composable
fun CardGrid(
    modifier: Modifier = Modifier,
    columns: Int = CardGridColumns,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
