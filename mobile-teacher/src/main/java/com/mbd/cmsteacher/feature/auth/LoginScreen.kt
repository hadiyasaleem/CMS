package com.mbd.cmsteacher.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.auth.RoleLoginScreen

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    RoleLoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onSendPasswordReset = { viewModel.sendPasswordReset { } },
        onLoginSuccess = onLoginSuccess,
        portalEyebrow = "Faculty Portal",
        screenTitle = "Teacher login",
        brandDescription = "Faculty workspace — attendance, marks, schedule & student records.",
        systemLabel = "GGC-MBD - TEACHER PORTAL",
        emailLabel = "Email Address",
        emailPlaceholder = "teacher@ggcmbd.edu.pk",
        footerText = "Teacher accounts are created by an administrator. Self-registration isn't available.",
    )
}
