package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.util.Outcome

@Composable
fun <T> CrudListScaffold(
    title: String,
    items: Outcome<List<T>>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    onAdd: (() -> Unit)? = null,
    emptyMessage: String = "Nothing here yet.",
    addLabel: String? = null,
    header: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (onAdd != null) {
                CmsFab(onAdd, contentDescription = addLabel ?: "Add")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding)) {
            SectionHeader(title, eyebrow)
            if (header != null) {
                header()
                Spacer(Modifier.height(8.dp))
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (items) {
                    is Outcome.Loading -> SkeletonList()
                    is Outcome.Error -> ErrorBanner(items.message, onRetry = onRefresh)
                    is Outcome.Success -> {
                        if (items.data.isEmpty()) {
                            EmptyState(
                                message = emptyMessage,
                                actionLabel = if (onAdd != null) (addLabel ?: "Add") else null,
                                onAction = onAdd,
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp),
                            ) {
                                items(items.data, key = itemKey) { item ->
                                    itemContent(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
