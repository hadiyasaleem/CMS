package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.auth.normalizeEmail
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.userMessage
import com.mbd.cmsdesktop.auth.DesktopRoleResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Student-only sign-in/registration state holder for [StudentAuthScreen]. Unlike [com.mbd.cmsdesktop.ui.login.LoginController]
 * (shared, sign-in-only, used by all 3 desktop apps), this one also supports account *registration*
 * - the student desktop app is the sole desktop app with a self-serve signup path, matching mobile's
 * `AuthViewModel`. A freshly registered account starts as [UserRole.UnlinkedStudent] until a link
 * request (see [StudentLinkRequestScreen]) gets approved.
 */
class StudentAuthController(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val roleResolver: DesktopRoleResolver,
    private val scope: CoroutineScope,
) {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isRegisterMode by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var resetMessage by mutableStateOf<String?>(null)
        private set

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
        errorMessage = null
        resetMessage = null
    }

    fun updateRegisterMode(value: Boolean) {
        if (value != isRegisterMode) toggleMode()
    }

    fun updateEmail(value: String) {
        email = value
        errorMessage = null
        resetMessage = null
    }

    fun updatePassword(value: String) {
        password = value
        errorMessage = null
    }

    fun submit(onResolved: (UserRole) -> Unit) {
        val validation = FieldValidators.emailError(email)
            ?: if (isRegisterMode) FieldValidators.passwordError(password) else (if (password.isEmpty()) "Password is required." else null)
        if (validation != null) {
            errorMessage = validation
            return
        }
        scope.launch {
            loading = true
            errorMessage = null
            try {
                if (isRegisterMode) {
                    sessionManager.registerStudent(email.normalizeEmail(), password)
                    val accountKey = sessionManager.accountKey ?: error("Registered but no email on account")
                    userRepository.provisionUnlinkedStudent(accountKey)
                    onResolved(UserRole.UnlinkedStudent(accountKey))
                } else {
                    sessionManager.signIn(email.normalizeEmail(), password)
                    val accountKey = sessionManager.accountKey ?: error("Signed in but no email on account")
                    val role = userRepository.resolveRole(accountKey)
                    if (role !is UserRole.LinkedStudent && role !is UserRole.UnlinkedStudent) {
                        sessionManager.signOut()
                        error("This account is not a Student account")
                    }
                    userRepository.touchLastLogin(accountKey)
                    onResolved(role)
                }
            } catch (t: Throwable) {
                errorMessage = t.userMessage()
            } finally {
                loading = false
            }
        }
    }

    fun sendPasswordReset() {
        if (FieldValidators.emailError(email) != null) {
            errorMessage = "Enter your email above first"
            return
        }
        scope.launch {
            errorMessage = null
            resetMessage = null
            try {
                sessionManager.sendPasswordReset(email)
                resetMessage = "Password reset email sent."
            } catch (t: Throwable) {
                errorMessage = t.userMessage("Could not send the reset email.")
            }
        }
    }
}
