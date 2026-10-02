package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.AppLogsController
import com.mbd.cmscommon.domain.repository.AppLogRepository
import com.mbd.cmscommon.ui.components.AppLogsWorkspace

@Composable
fun AppLogsScreen(repository: AppLogRepository) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository) { AppLogsController(repository, scope) }
    val logs by controller.logs.collectAsState()
    val loading by controller.loading.collectAsState()
    val deleting by controller.deleting.collectAsState()
    val errorMessage by controller.error.collectAsState()

    AppLogsWorkspace(
        logs = logs,
        loading = loading,
        deleting = deleting,
        errorMessage = errorMessage,
        onRefresh = controller::refresh,
        onStatusChange = { log, status -> controller.updateStatus(log.logId, status) },
        onDeleteAll = controller::deleteAll,
        onClearError = controller::clearError,
    )
}
