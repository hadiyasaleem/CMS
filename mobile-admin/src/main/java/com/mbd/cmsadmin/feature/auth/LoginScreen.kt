package com.mbd.cmsadmin.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.auth.RoleLoginScreen
import com.mbd.cmscommon.ui.theme.CmsTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CmsTheme.colors.ink)
            .statusBarsPadding(),
    ) {
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
}
