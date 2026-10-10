package com.mbd.cmsdesktop.ui.login

import com.mbd.cmscommon.util.userMessageLogged
import com.mbd.cmscommon.util.ErrorClassifier
import com.mbd.cmscommon.util.CmsException
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
    var resetCodeStep by mutableStateOf(false)
        private set
    var resetToken by mutableStateOf("")
    var resetNewPassword by mutableStateOf("")
    var resetConfirmPassword by mutableStateOf("")
    var resetConfirming by mutableStateOf(false)
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
            var step = "sign in"
            try {
                sessionManager.signIn(email.normalizeEmail(), password)
                step = "load your account details after signing in"
                val accountKey = sessionManager.accountKey
                    ?: throw CmsException.Auth("You signed in, but this account has no email address on record. Contact the college administrator.")
                val role = userRepository.resolveRole(accountKey)
                if (isAccepted(role)) {
                    userRepository.touchLastLogin(accountKey)
                    onResolved(role)
                } else {
                    sessionManager.signOut()
                    errorMessage = wrongRoleMessage
                }
            } catch (t: Throwable) {
                errorMessage = t.userMessageLogged("LoginController.submit", ErrorClassifier.fallbackFor(step))
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
                resetCodeStep = true
            } catch (t: Throwable) {
                errorMessage = t.userMessageLogged("LoginController.sendPasswordReset", "Couldn't send the password reset email to ${email.trim()}.")
            } finally {
                resetLoading = false
            }
        }
    }

    fun cancelPasswordReset() {
        resetCodeStep = false
        resetToken = ""
        resetNewPassword = ""
        resetConfirmPassword = ""
        resetMessage = null
    }

    /** Verifies the emailed code, sets the new password, then resolves the role and finishes sign-in
     * the same way [submit] does. */
    fun confirmPasswordReset(isAccepted: (UserRole) -> Boolean, wrongRoleMessage: String, onResolved: (UserRole) -> Unit) {
        val validation = FieldValidators.passwordConfirmationError(resetNewPassword, resetConfirmPassword)
            ?: FieldValidators.passwordError(resetNewPassword)
            ?: if (resetToken.isBlank()) "Enter the code we emailed you." else null
        if (validation != null) {
            resetMessage = validation
            return
        }
        val normalizedEmail = email.normalizeEmail()
        scope.launch {
            resetConfirming = true
            resetMessage = null
            try {
                sessionManager.confirmPasswordReset(normalizedEmail, resetToken, resetNewPassword)
                val accountKey = sessionManager.accountKey
                    ?: throw CmsException.Auth("The code was verified, but this account has no email address on record. Contact the college administrator.")
                val role = userRepository.resolveRole(accountKey)
                if (isAccepted(role)) {
                    userRepository.touchLastLogin(accountKey)
                    cancelPasswordReset()
                    onResolved(role)
                } else {
                    sessionManager.signOut()
                    resetMessage = wrongRoleMessage
                }
            } catch (t: Throwable) {
                resetMessage = t.userMessageLogged("LoginController.confirmPasswordReset", "Couldn't reset your password. Check the code and try again.")
            } finally {
                resetConfirming = false
            }
        }
    }
}
