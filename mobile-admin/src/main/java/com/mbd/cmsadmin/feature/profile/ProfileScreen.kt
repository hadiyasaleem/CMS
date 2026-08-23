package com.mbd.cmsadmin.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.AdministratorProfileWorkspace
import com.mbd.cmscommon.domain.model.administratorDirectorySnapshot

@Composable
fun ProfileScreen(onSignedOut: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val account by viewModel.account.collectAsState()
    val administrators by viewModel.administrators.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()

    AdministratorProfileWorkspace(
        accountKey = viewModel.accountKey,
        account = account,
        directory = administratorDirectorySnapshot(administrators),
        loading = loading,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = viewModel::refresh,
        onResetPassword = viewModel::resetPassword,
        onSignOut = {
            viewModel.signOut()
            onSignedOut()
        },
    )
}
