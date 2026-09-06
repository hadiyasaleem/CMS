package com.mbd.cmsstudent.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentAuthActions
import com.mbd.cmscommon.ui.components.StudentAuthWorkspace
import com.mbd.cmscommon.ui.theme.CmsTheme

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CmsTheme.colors.ink)
            .statusBarsPadding(),
    ) {
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
}
