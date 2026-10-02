package com.mbd.cmsadmin.feature.applogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.AppLogsWorkspace

@Composable
fun AppLogsScreen(
    refreshVersion: Int = 0,
    viewModel: AppLogsViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > 0) viewModel.refresh()
    }

    AppLogsWorkspace(
        logs = logs,
        loading = loading,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onStatusChange = { log, status -> viewModel.updateStatus(log.logId, status) },
        onClearError = viewModel::clearError,
    )
}
