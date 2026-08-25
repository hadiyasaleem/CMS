package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.util.Outcome
import java.util.Locale

data class DataColumn(
    val title: String,
    val width: Dp,
    val numeric: Boolean = false,
    val sortKey: String? = null,
)

@Composable
fun <T> DataTable(
    columns: List<DataColumn>,
    items: List<T>,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    sortColumn: String? = null,
    sortAscending: Boolean = true,
    onSort: ((String) -> Unit)? = null,
    onRowClick: ((T) -> Unit)? = null,
    cell: @Composable (Int, T) -> Unit,
) {
    val hScroll = rememberScrollState()
    val rowWidth = columns.fold(0.dp) { acc, c -> acc + c.width }

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .background(CmsTheme.colors.ink)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .width(rowWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            columns.forEach { col ->
                val isSortColumn = col.sortKey != null && col.sortKey == sortColumn
                Row(
                    modifier = Modifier
                        .width(col.width)
                        .then(
                            if (col.sortKey != null && onSort != null) {
                                Modifier.clickable { onSort(col.sortKey) }
                            } else {
                                Modifier
                            },
                        ),
                    horizontalArrangement = if (col.numeric) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        col.title.uppercase(Locale.ROOT),
                        color = if (isSortColumn) CmsTheme.colors.accent else CmsTheme.colors.onInk,
                        textAlign = if (col.numeric) TextAlign.End else TextAlign.Start,
                        style = CmsTextStyles.eyebrow,
                    )
                    if (isSortColumn) {
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            if (sortAscending) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            modifier = Modifier.heightIn(max = 12.dp),
                            tint = CmsTheme.colors.accent,
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 2.dp, color = CmsTheme.colors.rule)
        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(items, key = key) { item ->
                Column {
                    Row(
                        modifier = Modifier
                            .then(if (onRowClick != null) Modifier.clickable { onRowClick(item) } else Modifier)
                            .horizontalScroll(hScroll)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .width(rowWidth)
                            .heightIn(min = 32.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        columns.forEachIndexed { index, col ->
                            Box(Modifier.width(col.width)) {
                                cell(index, item)
                            }
                        }
                    }
                    HorizontalDivider(color = CmsTheme.colors.rule.copy(alpha = 0.35f))
                }
            }
        }
    }
}

@Composable
fun <T> DataTableScaffold(
    title: String,
    columns: List<DataColumn>,
    items: Outcome<List<T>>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    rowKey: (T) -> Any,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onAdd: (() -> Unit)? = null,
    onRowClick: ((T) -> Unit)? = null,
    emptyMessage: String = "Nothing here yet.",
    sortColumn: String? = null,
    sortAscending: Boolean = true,
    onSort: ((String) -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    cell: @Composable (Int, T) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (onAdd != null) {
                CmsFab(onAdd)
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding)) {
            SectionHeader(title, eyebrow)
            when (items) {
                is Outcome.Loading -> SkeletonList(modifier = Modifier.weight(1f))
                is Outcome.Error -> ErrorBanner(items.message, modifier = Modifier.weight(1f), onRetry = onRefresh)
                is Outcome.Success -> {
                    if (items.data.isEmpty()) {
                        EmptyState(
                            message = emptyMessage,
                            modifier = Modifier.weight(1f),
                            actionLabel = if (onAdd != null) "Add" else null,
                            onAction = onAdd,
                        )
                    } else {
                        if (header != null) {
                            header()
                            Spacer(Modifier.height(12.dp))
                        }
                        DataTable(
                            columns = columns,
                            items = items.data,
                            key = rowKey,
                            modifier = Modifier.weight(1f),
                            sortColumn = sortColumn,
                            sortAscending = sortAscending,
                            onSort = onSort,
                            onRowClick = onRowClick,
                            cell = cell,
                        )
                        actions?.invoke()
                    }
                }
            }
        }
    }
}

@Composable
fun TableCellText(text: String, numeric: Boolean = false, emphasis: Boolean = false) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = if (numeric) TextAlign.End else TextAlign.Start,
        style = if (emphasis) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
    )
}
