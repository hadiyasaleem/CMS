package com.mbd.cmscommon.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.Outcome

@Composable
fun RoleLoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSendPasswordReset: () -> Unit,
    onLoginSuccess: () -> Unit,
    portalEyebrow: String,
    screenTitle: String,
    brandDescription: String,
    systemLabel: String,
    emailLabel: String,
    emailPlaceholder: String,
    footerText: String,
) {
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.submitState) {
        if (uiState.submitState is Outcome.Success) onLoginSuccess()
    }

    Column(Modifier.fillMaxSize().background(CmsTheme.colors.faint).verticalScroll(rememberScrollState())) {
        NavyBrandPanel(collegeName = screenTitle, description = brandDescription, systemLabel = systemLabel)
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(portalEyebrow, color = CmsTheme.colors.accent, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(16.dp))
            CmsTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = emailLabel,
                placeholder = emailPlaceholder,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            val submitting = uiState.submitState is Outcome.Loading
            if (uiState.submitState is Outcome.Error) {
                Spacer(Modifier.height(8.dp))
                Text(uiState.submitState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (submitting) "Signing in…" else "Login",
                onClick = onSubmit,
                enabled = !submitting && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (submitting) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }

            Spacer(Modifier.height(12.dp))
            val resetting = uiState.resetState is Outcome.Loading
            TextButton(onClick = onSendPasswordReset, enabled = !resetting) {
                Text(if (resetting) "Sending reset email..." else "Forgot password?")
            }
            if (uiState.resetState is Outcome.Success) {
                Text("Password reset email sent.", color = CmsTheme.colors.success, style = MaterialTheme.typography.bodySmall)
            } else if (uiState.resetState is Outcome.Error) {
                Text(uiState.resetState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Text(footerText, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
