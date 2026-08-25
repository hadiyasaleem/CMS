package com.mbd.cmsdesktop.ui.login

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.Eyebrow
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.CollegeInfo
import com.mbd.cmscommon.util.FieldValidators
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

    val emailError = if (controller.email.isNotBlank()) FieldValidators.emailError(controller.email) else null
    val formValid = FieldValidators.emailError(controller.email) == null && controller.password.isNotEmpty()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        NavyBrandPanel(collegeName = CollegeInfo.NAME, description = brandDescription, systemLabel = systemLabel)
        Spacer(28.dp)
        Eyebrow(portalEyebrow)
        Spacer(8.dp)
        Text(screenTitle, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.displaySmall)
        Spacer(24.dp)
        CmsTextField(
            value = controller.email,
            onValueChange = { controller.email = it },
            label = emailLabel,
            placeholder = emailPlaceholder,
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            supportingText = emailError,
        )
        Spacer(16.dp)
        CmsTextField(
            value = controller.password,
            onValueChange = { controller.password = it },
            label = "Password",
            isPassword = !showPassword,
            keyboardType = KeyboardType.Password,
            isError = controller.errorMessage != null,
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
        controller.errorMessage?.let {
            Spacer(12.dp)
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        controller.resetMessage?.let {
            Spacer(12.dp)
            Text(it, color = CmsTheme.colors.success, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(24.dp)
        CmsPrimaryButton(
            text = if (controller.loading) "Signing in..." else "Login",
            onClick = { controller.submit(isAccepted, wrongRoleMessage, onResolved) },
            enabled = formValid && !controller.loading,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = controller::sendPasswordReset, enabled = !controller.resetLoading) {
            Text(
                if (controller.resetLoading) "Sending reset email..." else "Forgot password?",
                color = CmsTheme.colors.accent,
            )
        }
        Spacer(12.dp)
        Text(footerText, modifier = Modifier.fillMaxWidth(), color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodyMedium)
        if (controller.loading) {
            Spacer(8.dp)
            CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun Spacer(height: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(Modifier.height(height))
}

@Composable
private fun Spacer(height: Int) = Spacer(height.dp)
