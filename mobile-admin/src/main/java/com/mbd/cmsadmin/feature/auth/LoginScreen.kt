package com.mbd.cmsadmin.feature.auth

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
        portalEyebrow = "Security Portal",
        screenTitle = "Admin login",
        brandDescription = "Central console — enrolment, faculty, attendance, examinations & records.",
        systemLabel = "GGC-MBD - ADMIN PORTAL",
        emailLabel = "Email Address",
        emailPlaceholder = "admin@ggcmbd.edu.pk",
        footerText = "Admin accounts are created by another administrator. Self-registration isn't available.",
    )
}
