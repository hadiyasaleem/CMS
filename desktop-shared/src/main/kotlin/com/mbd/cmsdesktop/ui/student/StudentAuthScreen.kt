package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.StudentAuthActions
import com.mbd.cmscommon.ui.components.StudentAuthUiState
import com.mbd.cmscommon.ui.components.StudentAuthWorkspace
import com.mbd.cmsdesktop.auth.DesktopRoleResolver

/**
 * Entry point for the student desktop app when no session is active: sign-in AND registration
 * (mobile parity - see `AuthViewModel`/`LoginScreen` in mobile-student), unlike the other two
 * desktop apps which only ever sign in through [com.mbd.cmsdesktop.ui.login.LoginScreen].
 */
@Composable
fun StudentAuthScreen(
    sessionManager: SessionManager,
    userRepository: UserRepository,
    roleResolver: DesktopRoleResolver,
    onResolved: (UserRole) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember { StudentAuthController(sessionManager, userRepository, roleResolver, scope) }

    val state = StudentAuthUiState(
        email = controller.email,
        password = controller.password,
        registerMode = controller.isRegisterMode,
        loading = controller.loading,
        errorMessage = controller.errorMessage,
        resetSending = controller.resetSending,
        resetMessage = controller.resetMessage,
        resetError = controller.resetError,
    )
    val actions = StudentAuthActions(
        onEmailChange = controller::updateEmail,
        onPasswordChange = controller::updatePassword,
        onModeChange = controller::updateRegisterMode,
        onSubmit = { controller.submit(onResolved) },
        onPasswordReset = controller::sendPasswordReset,
    )
    StudentAuthWorkspace(state = state, actions = actions)
}
