package com.mbd.cmsdesktop.ui.login

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
 * Hand-rolled (no Hilt) login state holder shared by all 3 desktop apps' [LoginScreen]. [roleResolver]
 * is accepted for parity with the constructor shape but isn't invoked here - role resolution on
 * desktop happens entirely through [UserRepository.resolveRole], which already wraps
 * [DesktopRoleResolver]'s Postgrest lookups on the repository side.
 */
class LoginController(
    private val sessionManager: SessionManager,
    private val roleResolver: DesktopRoleResolver,
    private val userRepository: UserRepository,
    private val scope: CoroutineScope,
) {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var resetMessage by mutableStateOf<String?>(null)
    var resetLoading by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set

    fun submit(isAccepted: (UserRole) -> Boolean, wrongRoleMessage: String, onResolved: (UserRole) -> Unit) {
        val validation = FieldValidators.emailError(email) ?: if (password.isEmpty()) "Password is required." else null
        if (validation != null) {
            errorMessage = validation
            return
        }
        scope.launch {
            loading = true
            errorMessage = null
            try {
                sessionManager.signIn(email.normalizeEmail(), password)
                val accountKey = sessionManager.accountKey ?: error("Signed in but no email on account")
                val role = userRepository.resolveRole(accountKey)
                if (isAccepted(role)) {
                    userRepository.touchLastLogin(accountKey)
                    onResolved(role)
                } else {
                    sessionManager.signOut()
                    errorMessage = wrongRoleMessage
                }
            } catch (t: Throwable) {
                errorMessage = t.userMessage("Sign-in failed. Please try again.")
            } finally {
                loading = false
            }
        }
    }

    fun sendPasswordReset() {
        if (FieldValidators.emailError(email) != null) {
            resetMessage = null
            errorMessage = "Enter a valid email above first"
            return
        }
        scope.launch {
            resetLoading = true
            resetMessage = null
            errorMessage = null
            try {
                sessionManager.sendPasswordReset(email.trim())
                resetMessage = "Password reset email sent."
            } catch (t: Throwable) {
                errorMessage = t.userMessage("Could not send the reset email.")
            } finally {
                resetLoading = false
            }
        }
    }
}
