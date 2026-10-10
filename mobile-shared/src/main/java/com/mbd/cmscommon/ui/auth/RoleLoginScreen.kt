package com.mbd.cmscommon.ui.auth

import compose.icons.TablerIcons
import compose.icons.tablericons.Eye
import compose.icons.tablericons.EyeOff
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.mbd.cmscommon.ui.components.BadgeTone
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.components.StatusBadge
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.PasswordRule

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
                        Icon(if (showPassword) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = "Toggle password visibility")
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

/** The reset-password step shown once [onSendPasswordReset] has emailed a code -- a separate
 * screen from [RoleLoginScreen], not an inline section of the sign-in form. */
@Composable
fun ResetPasswordScreen(
    uiState: LoginUiState,
    onResetTokenChange: (String) -> Unit,
    onResetNewPasswordChange: (String) -> Unit,
    onResetConfirmPasswordChange: (String) -> Unit,
    onConfirmPasswordReset: () -> Unit,
    onCancelPasswordReset: () -> Unit,
    onLoginSuccess: () -> Unit,
    systemLabel: String,
) {
    var showResetPassword by remember { mutableStateOf(false) }
    val confirming = uiState.resetState is Outcome.Loading

    LaunchedEffect(uiState.submitState) {
        if (uiState.submitState is Outcome.Success) onLoginSuccess()
    }

    Column(Modifier.fillMaxSize().background(CmsTheme.colors.faint).verticalScroll(rememberScrollState())) {
        NavyBrandPanel(
            collegeName = "Reset password",
            description = "Enter the code we emailed you along with a new password.",
            systemLabel = systemLabel,
        )
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            TextButton(onClick = onCancelPasswordReset, enabled = !confirming) { Text("← Back to sign in") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the 6-digit code we emailed to ${uiState.email} along with a new password.",
                color = CmsTheme.colors.muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.resetState is Outcome.Error) {
                Text(uiState.resetState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            CmsTextField(
                value = uiState.resetToken,
                onValueChange = onResetTokenChange,
                label = "6-digit code",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = uiState.resetNewPassword,
                onValueChange = onResetNewPasswordChange,
                label = "New password",
                isPassword = !showResetPassword,
                trailingIcon = {
                    IconButton(onClick = { showResetPassword = !showResetPassword }) {
                        Icon(if (showResetPassword) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldValidators.passwordRules(uiState.resetNewPassword).forEach { rule: PasswordRule ->
                    StatusBadge(rule.label, if (rule.passed) BadgeTone.Success else BadgeTone.Neutral)
                }
            }
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = uiState.resetConfirmPassword,
                onValueChange = onResetConfirmPasswordChange,
                label = "Confirm new password",
                isPassword = !showResetPassword,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (confirming) "Resetting password…" else "Reset password",
                onClick = onConfirmPasswordReset,
                enabled = !confirming && uiState.resetToken.isNotBlank() && uiState.resetNewPassword.isNotBlank() && uiState.resetConfirmPassword.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (confirming) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }
        }
    }
}
