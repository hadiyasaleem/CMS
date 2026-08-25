package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.administratorDirectorySnapshot
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.ui.components.AdministratorProfileWorkspace
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.launch

/**
 * Admin's own profile: directory stats, password reset, and sign-out. Builds its own tiny
 * refresh/reset flow directly against the repositories (the decompiled screen had no dedicated
 * controller class either - it kept `loading`/`error`/`actionMessage` as local state).
 */
@Composable
fun AdminProfileScreen(
    sessionManager: SessionManager,
    repository: AdministratorRepository,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val accountKey = sessionManager.accountKey.orEmpty()
    val administrators by repository.observeAdministrators().collectAsState(initial = emptyList())
    val account = remember(administrators, accountKey) {
        administrators.firstOrNull { it.email.equals(accountKey, ignoreCase = true) }
    }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    suspend fun refresh() {
        loading = true
        error = null
        try {
            repository.sync()
        } catch (t: Throwable) {
            error = t.userMessage()
        } finally {
            loading = false
        }
    }

    LaunchedEffect(repository) { refresh() }

    AdministratorProfileWorkspace(
        accountKey = accountKey,
        account = account,
        directory = administratorDirectorySnapshot(administrators),
        loading = loading,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = { scope.launch { refresh() } },
        onResetPassword = {
            scope.launch {
                error = null
                actionMessage = null
                try {
                    sessionManager.sendPasswordReset(accountKey)
                    actionMessage = "Password reset email sent to $accountKey."
                } catch (t: Throwable) {
                    error = t.userMessage("Could not send the password reset email.")
                }
            }
        },
        onSignOut = onSignOut,
    )
}
