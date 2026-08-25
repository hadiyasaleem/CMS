package com.mbd.cmsstudent.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentAuthActions
import com.mbd.cmscommon.ui.components.StudentAuthWorkspace

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    StudentAuthWorkspace(
        state = state,
        actions = StudentAuthActions(
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onModeChange = viewModel::onModeChange,
            onSubmit = viewModel::submit,
            onPasswordReset = viewModel::sendPasswordReset,
        ),
    )
}
