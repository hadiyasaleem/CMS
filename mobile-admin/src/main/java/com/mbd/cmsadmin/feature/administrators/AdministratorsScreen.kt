package com.mbd.cmsadmin.feature.administrators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.AdministratorDirectoryWorkspace

@Composable
fun AdministratorsScreen(viewModel: AdministratorsViewModel = hiltViewModel()) {
    val administrators by viewModel.administrators.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val creating by viewModel.creating.collectAsState()
    val createdEmail by viewModel.createdEmail.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    AdministratorDirectoryWorkspace(
        administrators = administrators,
        currentAccountKey = viewModel.currentAccountKey,
        loading = loading,
        creating = creating,
        createdEmail = createdEmail,
        errorMessage = errorMessage,
        onRefresh = viewModel::refresh,
        onCreate = viewModel::create,
        onConsumeCreated = viewModel::consumeCreated,
        onClearError = viewModel::clearError,
    )
}
