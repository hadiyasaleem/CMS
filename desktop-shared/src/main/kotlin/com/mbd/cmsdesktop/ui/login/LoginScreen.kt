package com.mbd.cmsdesktop.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.theme.CmsTheme
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

    Column(Modifier.fillMaxSize().background(CmsTheme.colors.faint).verticalScroll(rememberScrollState())) {
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
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
