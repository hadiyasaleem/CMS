package com.mbd.cmsdesktop.ui.login

import compose.icons.TablerIcons
import compose.icons.tablericons.Eye
import compose.icons.tablericons.EyeOff
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.BadgeTone
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.components.StatusBadge
import com.mbd.cmscommon.ui.components.WithVerticalScrollbar
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.PasswordRule
import com.mbd.cmsdesktop.auth.DesktopRoleResolver

/**
 * Shared role-locked login shell for all 3 desktop apps: hand-rolled [LoginController] (no Hilt)
 * instead of a mobile-style ViewModel. Each app's `Main.kt` supplies its own portal copy and
 * [isAccepted] predicate, and receives the resolved [UserRole] back through [onResolved].
 */
@Composable
fun LoginScreen(
    sessionManager: SessionManager,
    roleResolver: DesktopRoleResolver,
    userRepository: UserRepository,
    portalEyebrow: String,
    screenTitle: String,
    brandDescription: String,
    systemLabel: String,
    emailLabel: String,
    emailPlaceholder: String,
    footerText: String,
    isAccepted: (UserRole) -> Boolean,
    wrongRoleMessage: String,
    onResolved: (UserRole) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember { LoginController(sessionManager, roleResolver, userRepository, scope) }
    var showPassword by remember { mutableStateOf(false) }

    if (controller.resetCodeStep) {
        ResetPasswordScreen(controller = controller, systemLabel = systemLabel, isAccepted = isAccepted, wrongRoleMessage = wrongRoleMessage, onResolved = onResolved)
        return
    }

    val scrollState = rememberScrollState()
    WithVerticalScrollbar(scrollState, Modifier.fillMaxSize().background(CmsTheme.colors.faint)) {
    Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
        NavyBrandPanel(collegeName = screenTitle, description = brandDescription, systemLabel = systemLabel)
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(portalEyebrow, color = CmsTheme.colors.accent, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(16.dp))
            CmsTextField(
                value = controller.email,
                onValueChange = { controller.email = it },
                label = emailLabel,
                placeholder = emailPlaceholder,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = controller.password,
                onValueChange = { controller.password = it },
                label = "Password",
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) TablerIcons.EyeOff else TablerIcons.Eye,
                            contentDescription = "Toggle password visibility",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            controller.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (controller.loading) "Signing in..." else "Login",
                onClick = { controller.submit(isAccepted, wrongRoleMessage, onResolved) },
                enabled = !controller.loading && controller.email.isNotBlank() && controller.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (controller.loading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = controller::sendPasswordReset, enabled = !controller.resetLoading) {
                Text(if (controller.resetLoading) "Sending reset email..." else "Forgot password?")
            }
            controller.resetMessage?.let {
                Text(it, color = CmsTheme.colors.success, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                footerText,
                color = CmsTheme.colors.muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    }
}

/** The reset-password step shown once [LoginController.sendPasswordReset] has emailed a code --
 * a separate screen from the sign-in form, not an inline section of it. */
@Composable
private fun ResetPasswordScreen(
    controller: LoginController,
    systemLabel: String,
    isAccepted: (UserRole) -> Boolean,
    wrongRoleMessage: String,
    onResolved: (UserRole) -> Unit,
) {
    var showResetPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    WithVerticalScrollbar(scrollState, Modifier.fillMaxSize().background(CmsTheme.colors.faint)) {
    Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
        NavyBrandPanel(
            collegeName = "Reset password",
            description = "Enter the code we emailed you along with a new password.",
            systemLabel = systemLabel,
        )
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            TextButton(onClick = controller::cancelPasswordReset, enabled = !controller.resetConfirming) { Text("← Back to sign in") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Enter the 6-digit code we emailed to ${controller.email} along with a new password.",
                color = CmsTheme.colors.muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(16.dp))

            controller.resetMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            CmsTextField(
                value = controller.resetToken,
                onValueChange = { controller.resetToken = it },
                label = "6-digit code",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = controller.resetNewPassword,
                onValueChange = { controller.resetNewPassword = it },
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
                FieldValidators.passwordRules(controller.resetNewPassword).forEach { rule: PasswordRule ->
                    StatusBadge(rule.label, if (rule.passed) BadgeTone.Success else BadgeTone.Neutral)
                }
            }
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = controller.resetConfirmPassword,
                onValueChange = { controller.resetConfirmPassword = it },
                label = "Confirm new password",
                isPassword = !showResetPassword,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (controller.resetConfirming) "Resetting password…" else "Reset password",
                onClick = { controller.confirmPasswordReset(isAccepted, wrongRoleMessage, onResolved) },
                enabled = !controller.resetConfirming && controller.resetToken.isNotBlank() && controller.resetNewPassword.isNotBlank() && controller.resetConfirmPassword.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (controller.resetConfirming) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }
        }
    }
    }
}
