package com.mbd.cmsdesktop.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.mbd.cmscommon.auth.RoleResolver
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.NavyBrandPanel
import com.mbd.cmscommon.ui.theme.CmsTheme
import kotlinx.coroutines.launch

/**
 * Shared role-locked login shell for all 3 desktop apps (mirrors mobile's `RoleLoginScreen`, but
 * hand-rolled state instead of a Hilt ViewModel — desktop screens own their state locally). Each
 * app's `Main.kt` supplies its own copy, portal strings and [isAccepted] predicate.
 */
@Composable
fun LoginScreen(
    sessionManager: SessionManager,
    roleResolver: RoleResolver,
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (busy) return
        error = null
        busy = true
        scope.launch {
            try {
                sessionManager.signIn(email, password)
                val uid = sessionManager.accountKey ?: error("Sign-in did not return an account")
                val role = userRepository.resolveRole(uid)
                if (isAccepted(role)) {
                    userRepository.touchLastLogin(uid)
                    onResolved(role)
                } else {
                    sessionManager.signOut()
                    error = wrongRoleMessage
                }
            } catch (t: Throwable) {
                error = t.message ?: "Sign-in failed. Check your credentials and try again."
            } finally {
                busy = false
            }
        }
    }

    Row(Modifier.fillMaxSize()) {
        NavyBrandPanel(
            collegeName = "GGC-MBD",
            description = brandDescription,
            systemLabel = systemLabel,
            modifier = Modifier.fillMaxHeight().width(360.dp),
        )
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(Modifier.widthIn(max = 360.dp).padding(32.dp)) {
                Text(portalEyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = CmsTheme.colors.muted)
                Spacer(Modifier.height(8.dp))
                Text(screenTitle, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(28.dp))
                CmsTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = emailLabel,
                    placeholder = emailPlaceholder,
                    keyboardType = KeyboardType.Email,
                    isError = error != null,
                )
                Spacer(Modifier.height(16.dp))
                CmsTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    isError = error != null,
                    supportingText = error,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = ::submit,
                    enabled = !busy && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (busy) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Sign in")
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = { scope.launch { runCatching { sessionManager.sendPasswordReset(email) } } },
                    enabled = email.isNotBlank(),
                ) {
                    Text("Forgot password?")
                }
                Spacer(Modifier.height(24.dp))
                Text(footerText, style = MaterialTheme.typography.bodySmall, color = CmsTheme.colors.muted)
            }
        }
    }
}
