package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.CollegeInfo

data class StudentAuthUiState(
    val email: String = "",
    val password: String = "",
    val registerMode: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
)

data class StudentAuthActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onModeChange: (Boolean) -> Unit,
    val onSubmit: () -> Unit,
    val onPasswordReset: () -> Unit,
)

/** Mirrors [com.mbd.cmscommon.ui.auth.RoleLoginScreen]'s layout (navy brand hero, flat form,
 * no card) so the student portal's sign-in/register screen matches admin and teacher. */
@Composable
fun StudentAuthWorkspace(state: StudentAuthUiState, actions: StudentAuthActions, modifier: Modifier = Modifier) {
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().background(CmsTheme.colors.faint).verticalScroll(rememberScrollState())) {
        NavyBrandPanel(
            collegeName = "Student Portal",
            description = "Attendance, marks, timetable and fee records in one secure student portal.",
            systemLabel = "GGC-MBD - STUDENT PORTAL",
        )
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                if (state.registerMode) "Create your account" else "Welcome back",
                color = CmsTheme.colors.accent,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(4.dp))
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
            if (!state.noticeMessage.isNullOrBlank()) {
                Text(state.noticeMessage, color = CmsTheme.colors.success, style = MaterialTheme.typography.bodySmall)
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
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (state.loading) "Please wait…" else if (state.registerMode) "Create account" else "Sign in",
                onClick = actions.onSubmit,
                enabled = !state.loading && state.email.isNotBlank() && state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.loading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { actions.onModeChange(!state.registerMode) }) {
                Text(if (state.registerMode) "Login instead" else "Register instead")
            }
            if (!state.registerMode) {
                TextButton(onClick = actions.onPasswordReset) { Text("Forgot password?") }
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
