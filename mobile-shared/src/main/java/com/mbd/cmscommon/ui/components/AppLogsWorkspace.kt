package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Search
import compose.icons.tablericons.Trash
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AppLogRecord
import com.mbd.cmscommon.domain.model.AppLogStatus
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val LogDateFormat = DateTimeFormatter.ofPattern("dd MMM, HH:mm")

/**
 * The admin app's "App Logs" screen: critical/crash failures reported by every client (mobile +
 * desktop, every role), downloaded from the server's `app_logs` table and cached locally so the
 * list is still readable offline. Every log starts [AppLogStatus.NEW]; an admin triages it through
 * [AppLogStatus.IN_PROGRESS] to [AppLogStatus.FIXED] (or reopens a fix that didn't hold) via [onStatusChange],
 * one tab per status.
 */
@Composable
fun AppLogsWorkspace(
    logs: List<AppLogRecord>,
    loading: Boolean,
    deleting: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onStatusChange: (AppLogRecord, AppLogStatus) -> Unit,
    onDeleteAll: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var appFilter by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(AppLogStatus.NEW) }
    var confirmingDeleteAll by remember { mutableStateOf(false) }

    val apps = remember(logs) { logs.mapNotNull { it.appId }.distinct().sorted() }
    val countsByStatus = remember(logs) { logs.groupingBy { it.status }.eachCount() }

    val filtered = logs.filter { log ->
        val matchesQuery = query.isBlank() ||
            log.message.contains(query, ignoreCase = true) ||
            log.tag?.contains(query, ignoreCase = true) == true ||
            log.accountEmail?.contains(query, ignoreCase = true) == true
        val matchesApp = appFilter == null || log.appId == appFilter
        log.status == tab && matchesQuery && matchesApp
    }

    RefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = modifier) {
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                AppLogsHero(
                    count = logs.size,
                    onDeleteAll = { confirmingDeleteAll = true },
                    deleteEnabled = logs.isNotEmpty() && !deleting,
                )
            }

            item {
                TabRow(selectedTabIndex = AppLogStatus.entries.indexOf(tab)) {
                    AppLogStatus.entries.forEach { status ->
                        Tab(
                            selected = tab == status,
                            onClick = { tab = status },
                            text = { Text("${status.label} (${countsByStatus[status] ?: 0})") },
                        )
                    }
                }
            }

            item {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search message, tag, or account") },
                        leadingIcon = { Icon(TablerIcons.Search, contentDescription = null) },
                        singleLine = true,
                    )
                    if (apps.size > 1) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CmsChip("All apps", selected = appFilter == null, onClick = { appFilter = null })
                            apps.forEach { app -> CmsChip(app, selected = appFilter == app, onClick = { appFilter = app }) }
                        }
                    }
                }
            }

            if (!loading && logs.isEmpty()) {
                item { AppLogsEmpty("No logs recorded", "Nothing unexpected has been reported by any app yet.") }
            } else if (filtered.isEmpty()) {
                item { AppLogsEmpty("No matching logs", "Try a different search or app filter, or check another tab.") }
            } else {
                items(filtered, key = { it.logId }) { log -> AppLogCard(log, onStatusChange = { status -> onStatusChange(log, status) }) }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (!errorMessage.isNullOrBlank()) {
        CmsErrorDialog(message = errorMessage, title = "Couldn't refresh app logs", onDismiss = onClearError)
    }

    if (confirmingDeleteAll) {
        ConfirmDestructiveActionDialog(
            title = "Clear all app logs?",
            dependentSummary = "${logs.size} log entr${if (logs.size == 1) "y" else "ies"} will be cleared from this list for every admin.",
            confirmLabel = if (deleting) "Clearing..." else "Clear all logs",
            onConfirm = { onDeleteAll(); confirmingDeleteAll = false },
            onDismiss = { confirmingDeleteAll = false },
        )
    }
}

@Composable
private fun AppLogsHero(count: Int, onDeleteAll: () -> Unit, deleteEnabled: Boolean) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DIAGNOSTICS", color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text("App Logs", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
                TextButton(onClick = onDeleteAll, enabled = deleteEnabled) {
                    Icon(TablerIcons.Trash, contentDescription = null, tint = if (deleteEnabled) CmsTheme.colors.accent else ModMuted)
                    Spacer(Modifier.width(4.dp))
                    Text("Clear all", color = if (deleteEnabled) CmsTheme.colors.accent else ModMuted)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("$count unexpected failures recorded across every app", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AppLogCard(log: AppLogRecord, onStatusChange: (AppLogStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row {
                StatusBadge(log.tag?.uppercase() ?: (log.kind ?: "UNEXPECTED"), BadgeTone.Error)
                Spacer(Modifier.width(8.dp))
                Text(
                    log.occurredAt.atZone(ZoneId.systemDefault()).format(LogDateFormat),
                    color = ModMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(log.message, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                listOfNotNull(
                    log.accountEmail,
                    log.appId?.let { id -> "$id${log.appVersion?.let { " v$it" } ?: ""}" },
                    log.platform,
                    log.deviceInfo,
                ).joinToString(" · "),
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            val stackTrace = log.stackTrace
            if (!stackTrace.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(if (expanded) "Hide stack trace" else "Show stack trace", color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                    Icon(
                        if (expanded) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                        contentDescription = null,
                        tint = CmsTheme.colors.accent,
                        modifier = Modifier.height(16.dp),
                    )
                }
                if (expanded) {
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = ModInk) {
                        Text(
                            stackTrace,
                            modifier = Modifier.padding(12.dp),
                            color = CmsTheme.colors.onInk,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                when (log.status) {
                    AppLogStatus.NEW -> TextButton(onClick = { onStatusChange(AppLogStatus.IN_PROGRESS) }) { Text("Start work") }
                    AppLogStatus.IN_PROGRESS -> {
                        TextButton(onClick = { onStatusChange(AppLogStatus.FIXED) }) { Text("Mark fixed") }
                        TextButton(onClick = { onStatusChange(AppLogStatus.NEW) }) { Text("Back to new") }
                    }
                    AppLogStatus.FIXED -> TextButton(onClick = { onStatusChange(AppLogStatus.NEW) }) { Text("Reopen") }
                }
            }
        }
    }
}

@Composable
private fun AppLogsEmpty(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Icon(TablerIcons.AlertTriangle, contentDescription = null, tint = ModMuted)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
