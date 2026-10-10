package com.mbd.cmscommon.ui.components

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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.CollegeInfo
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.PasswordRule

data class StudentAuthUiState(
    val email: String = "",
    val password: String = "",
    val registerMode: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val resetSending: Boolean = false,
    val resetMessage: String? = null,
    val resetError: Boolean = false,
    val registerCooldownActive: Boolean = false,
    /** True once a reset code has been emailed -- reveals the code + new-password step below. */
    val resetCodeStep: Boolean = false,
    val resetToken: String = "",
    val resetNewPassword: String = "",
    val resetConfirmPassword: String = "",
    val resetConfirming: Boolean = false,
)

data class StudentAuthActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onModeChange: (Boolean) -> Unit,
    val onSubmit: () -> Unit,
    val onPasswordReset: () -> Unit,
    val onResetTokenChange: (String) -> Unit = {},
    val onResetNewPasswordChange: (String) -> Unit = {},
    val onResetConfirmPasswordChange: (String) -> Unit = {},
    val onConfirmPasswordReset: () -> Unit = {},
    val onCancelPasswordReset: () -> Unit = {},
)

/** Mirrors [com.mbd.cmscommon.ui.auth.RoleLoginScreen]'s layout (navy brand hero, flat form,
 * no card) so the student portal's sign-in/register screen matches admin and teacher. */
@Composable
fun StudentAuthWorkspace(state: StudentAuthUiState, actions: StudentAuthActions, modifier: Modifier = Modifier) {
    var showPassword by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    WithVerticalScrollbar(scrollState, modifier.fillMaxSize().background(CmsTheme.colors.faint)) {
    Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
        NavyBrandPanel(
            collegeName = "Student Portal",
            description = "Attendance, marks, timetable and fee records in one secure student portal.",
            systemLabel = "GGC-MBD - STUDENT PORTAL",
        )
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            TabRow(
                selectedTabIndex = if (state.registerMode) 1 else 0,
                containerColor = Color.Transparent,
                contentColor = CmsTheme.colors.accent,
            ) {
                Tab(selected = !state.registerMode, onClick = { actions.onModeChange(false) }, text = { Text("Sign in") })
                Tab(selected = state.registerMode, onClick = { actions.onModeChange(true) }, text = { Text("Register") })
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (state.registerMode) {
                    "Use an email you can access. Your college record is linked after verification."
                } else {
                    "Sign in to continue to your academic workspace."
                },
                color = CmsTheme.colors.muted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(16.dp))

            if (!state.errorMessage.isNullOrBlank()) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            if (!state.infoMessage.isNullOrBlank()) {
                Text(state.infoMessage, color = CmsTheme.colors.success, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            CmsTextField(
                value = state.email,
                onValueChange = actions.onEmailChange,
                label = "Email address",
                placeholder = "you@example.com",
                supportingText = if (state.registerMode) "Use your personal or college email address." else null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = "Password",
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            val registerBlocked = state.registerMode && state.registerCooldownActive
            CmsPrimaryButton(
                text = if (state.loading) "Please wait…" else if (registerBlocked) "Verification link already sent" else if (state.registerMode) "Create account" else "Sign in",
                onClick = actions.onSubmit,
                enabled = !state.loading && !registerBlocked && state.email.isNotBlank() && state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.loading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }

            Spacer(Modifier.height(12.dp))
            if (!state.registerMode) {
                if (state.resetCodeStep) {
                    Text(
                        "Enter the 6-digit code we emailed to ${state.email} along with a new password.",
                        color = CmsTheme.colors.muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(10.dp))
                    CmsTextField(
                        value = state.resetToken,
                        onValueChange = actions.onResetTokenChange,
                        label = "6-digit code",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    CmsTextField(
                        value = state.resetNewPassword,
                        onValueChange = actions.onResetNewPasswordChange,
                        label = "New password",
                        isPassword = !showResetPassword,
                        trailingIcon = {
                            IconButton(onClick = { showResetPassword = !showResetPassword }) {
                                Icon(if (showResetPassword) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = "Toggle password visibility")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldValidators.passwordRules(state.resetNewPassword).forEach { rule: PasswordRule ->
                            StatusBadge(rule.label, if (rule.passed) BadgeTone.Success else BadgeTone.Neutral)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    CmsTextField(
                        value = state.resetConfirmPassword,
                        onValueChange = actions.onResetConfirmPasswordChange,
                        label = "Confirm new password",
                        isPassword = !showResetPassword,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    CmsPrimaryButton(
                        text = if (state.resetConfirming) "Resetting password…" else "Reset password",
                        onClick = actions.onConfirmPasswordReset,
                        enabled = !state.resetConfirming && state.resetToken.isNotBlank() && state.resetNewPassword.isNotBlank() && state.resetConfirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = actions.onCancelPasswordReset, enabled = !state.resetConfirming) { Text("Back to sign in") }
                } else {
                    TextButton(onClick = actions.onPasswordReset, enabled = !state.resetSending) {
                        Text(if (state.resetSending) "Sending code…" else "Forgot password?")
                    }
                }
                if (!state.resetMessage.isNullOrBlank()) {
                    Text(
                        state.resetMessage,
                        color = if (state.resetError) MaterialTheme.colorScheme.error else CmsTheme.colors.success,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                CollegeInfo.NAME,
                color = CmsTheme.colors.muted,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    }
}
