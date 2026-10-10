package com.mbd.cmsdesktop.ui.student

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
import com.mbd.cmscommon.util.userMessageLogged
import com.mbd.cmsdesktop.auth.DesktopRoleResolver
import java.util.prefs.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val REGISTER_COOLDOWN_MILLIS = 60L * 60L * 1000L
private const val LAST_REGISTER_EMAIL_KEY = "last_register_email"
private const val LAST_REGISTER_AT_MILLIS_KEY = "last_register_at_millis"

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
    var infoMessage by mutableStateOf<String?>(null)
        private set
    var resetSending by mutableStateOf(false)
        private set
    var resetMessage by mutableStateOf<String?>(null)
        private set
    var resetError by mutableStateOf(false)
        private set
    var registerCooldownActive by mutableStateOf(false)
        private set
    var resetCodeStep by mutableStateOf(false)
        private set
    var resetToken by mutableStateOf("")
        private set
    var resetNewPassword by mutableStateOf("")
        private set
    var resetConfirmPassword by mutableStateOf("")
        private set
    var resetConfirming by mutableStateOf(false)
        private set

    private val prefs: Preferences = Preferences.userRoot().node("com/mbd/cms/studentauth")

    /** True if [email] already has a pending, unexpired verification email on file. */
    private fun isOnRegisterCooldown(email: String): Boolean {
        val lastEmail = prefs.get(LAST_REGISTER_EMAIL_KEY, null)
        val lastAt = prefs.getLong(LAST_REGISTER_AT_MILLIS_KEY, 0L)
        return lastEmail == email && lastAt > 0L && System.currentTimeMillis() - lastAt < REGISTER_COOLDOWN_MILLIS
    }

    fun toggleMode() {
        isRegisterMode = !isRegisterMode
        errorMessage = null
        infoMessage = null
        resetMessage = null
        registerCooldownActive = isOnRegisterCooldown(email.normalizeEmail())
        cancelPasswordReset()
    }

    fun updateResetToken(value: String) { resetToken = value; resetMessage = null }
    fun updateResetNewPassword(value: String) { resetNewPassword = value; resetMessage = null }
    fun updateResetConfirmPassword(value: String) { resetConfirmPassword = value; resetMessage = null }
    fun cancelPasswordReset() {
        resetCodeStep = false
        resetToken = ""
        resetNewPassword = ""
        resetConfirmPassword = ""
        resetMessage = null
    }

    fun updateRegisterMode(value: Boolean) {
        if (value != isRegisterMode) toggleMode()
    }

    fun updateEmail(value: String) {
        email = value
        errorMessage = null
        infoMessage = null
        resetMessage = null
        registerCooldownActive = isOnRegisterCooldown(value.normalizeEmail())
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
        val normalizedEmail = email.normalizeEmail()
        if (isRegisterMode && isOnRegisterCooldown(normalizedEmail)) {
            errorMessage = "We already sent a verification link to $normalizedEmail. Check your inbox (and spam/junk folder), or try again in an hour."
            registerCooldownActive = true
            return
        }
        scope.launch {
            loading = true
            errorMessage = null
            infoMessage = null
            var step = if (isRegisterMode) "create your account" else "sign in"
            try {
                if (isRegisterMode) {
                    sessionManager.registerStudent(normalizedEmail, password)
                    step = "finish setting up your account"
                    val accountKey = sessionManager.accountKey
                    if (accountKey != null) {
                        // Email confirmation is disabled on this project -- a session exists immediately.
                        userRepository.provisionUnlinkedStudent(accountKey)
                        onResolved(UserRole.UnlinkedStudent(accountKey))
                    } else {
                        // Normal case: Supabase requires email confirmation before a session exists.
                        // The mobile app's AppRootViewModel-equivalent reactive hook isn't ported to
                        // desktop yet, so on desktop the student must complete registration on mobile
                        // (or come back and sign in here once the link is opened on the same device
                        // where the confirmation redirect can be handled).
                        prefs.put(LAST_REGISTER_EMAIL_KEY, normalizedEmail)
                        prefs.putLong(LAST_REGISTER_AT_MILLIS_KEY, System.currentTimeMillis())
                        registerCooldownActive = true
                        infoMessage = "We sent a verification link to $normalizedEmail. Open it, then come back and sign in. " +
                            "Don't see it? Check your spam or junk folder too."
                    }
                } else {
                    sessionManager.signIn(email.normalizeEmail(), password)
                    step = "finish setting up your account"
                    val accountKey = sessionManager.accountKey
                        ?: throw CmsException.Auth("You signed in, but this account has no email address on record. Contact the college administrator.")
                    val role = userRepository.resolveRole(accountKey)
                    if (role !is UserRole.LinkedStudent && role !is UserRole.UnlinkedStudent) {
                        sessionManager.signOut()
                        throw CmsException.Auth("This account is not a Student account.")
                    }
                    userRepository.touchLastLogin(accountKey)
                    onResolved(role)
                }
            } catch (t: Throwable) {
                errorMessage = t.userMessageLogged("StudentAuthController.submit", ErrorClassifier.fallbackFor(step))
            } finally {
                loading = false
            }
        }
    }

    fun sendPasswordReset() {
        if (FieldValidators.emailError(email) != null) {
            resetMessage = "Enter a valid email above first"
            resetError = true
            return
        }
        scope.launch {
            resetSending = true
            resetMessage = null
            try {
                sessionManager.sendPasswordReset(email)
                resetCodeStep = true
                resetMessage = null
                resetError = false
            } catch (t: Throwable) {
                resetMessage = t.userMessageLogged("StudentAuthController.sendPasswordReset", "Couldn't send the password reset email to ${email.trim()}.")
                resetError = true
            } finally {
                resetSending = false
            }
        }
    }

    /** Verifies the emailed code, sets the new password, and (unlike mobile, which has a reactive
     * hook for a session landing with no call site of its own) resolves the role explicitly here,
     * the same way [submit]'s sign-in branch does. */
    fun confirmPasswordReset(onResolved: (UserRole) -> Unit) {
        val validation = FieldValidators.passwordConfirmationError(resetNewPassword, resetConfirmPassword)
            ?: FieldValidators.passwordError(resetNewPassword)
            ?: if (resetToken.isBlank()) "Enter the code we emailed you." else null
        if (validation != null) {
            resetMessage = validation
            resetError = true
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
                if (role !is UserRole.LinkedStudent && role !is UserRole.UnlinkedStudent) {
                    sessionManager.signOut()
                    throw CmsException.Auth("This account is not a Student account.")
                }
                userRepository.touchLastLogin(accountKey)
                cancelPasswordReset()
                onResolved(role)
            } catch (t: Throwable) {
                resetMessage = t.userMessageLogged("StudentAuthController.confirmPasswordReset", "Couldn't reset your password. Check the code and try again.")
                resetError = true
            } finally {
                resetConfirming = false
            }
        }
    }
}
