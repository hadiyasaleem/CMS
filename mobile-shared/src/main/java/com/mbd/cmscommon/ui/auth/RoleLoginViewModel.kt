package com.mbd.cmscommon.ui.auth

import com.mbd.cmscommon.util.userMessageLogged
import com.mbd.cmscommon.util.ErrorClassifier
import com.mbd.cmscommon.util.CmsException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.auth.normalizeEmail
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val submitState: Outcome<Unit>? = null,
    val resetState: Outcome<Unit>? = null,
    /** True once a reset code has been emailed -- the caller shows [ResetPasswordScreen] instead
     * of [RoleLoginScreen] while this is set. */
    val resetCodeStep: Boolean = false,
    val resetToken: String = "",
    val resetNewPassword: String = "",
    val resetConfirmPassword: String = "",
)

abstract class RoleLoginViewModel(
    private val sessionManager: SessionManager,
    protected val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    protected abstract fun isAccepted(role: UserRole): Boolean
    protected abstract val wrongRoleMessage: String

    protected open suspend fun afterRoleResolved(accountKey: String, role: UserRole): UserRole = role

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun submit() {
        val state = _uiState.value
        val validation = FieldValidators.emailError(state.email)
            ?: if (state.password.isEmpty()) "Password is required." else null

        if (validation != null) {
            _uiState.value = state.copy(submitState = Outcome.Error(validation))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitState = Outcome.Loading)
            var step = "sign in"
            try {
                sessionManager.signIn(state.email.normalizeEmail(), state.password)
                step = "load your account details after signing in"
                val accountKey = sessionManager.accountKey
                    ?: throw CmsException.Auth("You signed in, but this account has no email address on record. Contact the college administrator.")
                val role = afterRoleResolved(accountKey, userRepository.resolveRole(accountKey))
                if (!isAccepted(role)) {
                    sessionManager.signOut()
                    _uiState.value = _uiState.value.copy(submitState = Outcome.Error(wrongRoleMessage))
                    return@launch
                }
                userRepository.touchLastLogin(accountKey)
                _uiState.value = _uiState.value.copy(submitState = Outcome.Success(Unit))
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(submitState = Outcome.Error(t.userMessageLogged("RoleLoginViewModel.submit", ErrorClassifier.fallbackFor(step)), t))
            }
        }
    }

    fun sendPasswordReset(onDone: (Outcome<Unit>) -> Unit) {
        val email = _uiState.value.email
        if (FieldValidators.emailError(email) != null) {
            val outcome = Outcome.Error("Enter a valid email above first")
            _uiState.value = _uiState.value.copy(resetState = outcome)
            onDone(outcome)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resetState = Outcome.Loading)
            val outcome = try {
                sessionManager.sendPasswordReset(email)
                Outcome.Success(Unit)
            } catch (t: Throwable) {
                Outcome.Error(t.userMessageLogged("RoleLoginViewModel.sendPasswordReset", "Couldn't send the password reset email to ${email.trim()}."), t)
            }
            _uiState.value = _uiState.value.copy(resetState = outcome, resetCodeStep = outcome is Outcome.Success)
            onDone(outcome)
        }
    }

    fun onResetTokenChange(value: String) { _uiState.value = _uiState.value.copy(resetToken = value) }
    fun onResetNewPasswordChange(value: String) { _uiState.value = _uiState.value.copy(resetNewPassword = value) }
    fun onResetConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(resetConfirmPassword = value) }
    fun cancelPasswordReset() {
        _uiState.value = _uiState.value.copy(resetState = null, resetCodeStep = false, resetToken = "", resetNewPassword = "", resetConfirmPassword = "")
    }

    /** Verifies the emailed code, sets the new password, then resolves the role and finishes sign-in
     * the same way [submit] does -- there's no reactive "a session landed with no call site of its
     * own" hook here (that's mobile-student's AppRootViewModel only), so this does it explicitly. */
    fun confirmPasswordReset() {
        val state = _uiState.value
        val email = state.email.normalizeEmail()
        val validation = FieldValidators.passwordConfirmationError(state.resetNewPassword, state.resetConfirmPassword)
            ?: FieldValidators.passwordError(state.resetNewPassword)
            ?: if (state.resetToken.isBlank()) "Enter the code we emailed you." else null
        if (validation != null) {
            _uiState.value = state.copy(resetState = Outcome.Error(validation))
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resetState = Outcome.Loading)
            var step = "reset your password"
            try {
                sessionManager.confirmPasswordReset(email, state.resetToken, state.resetNewPassword)
                step = "load your account details after resetting your password"
                val accountKey = sessionManager.accountKey
                    ?: throw CmsException.Auth("The code was verified, but this account has no email address on record. Contact the college administrator.")
                val role = afterRoleResolved(accountKey, userRepository.resolveRole(accountKey))
                if (!isAccepted(role)) {
                    sessionManager.signOut()
                    _uiState.value = _uiState.value.copy(resetState = Outcome.Error(wrongRoleMessage))
                    return@launch
                }
                userRepository.touchLastLogin(accountKey)
                _uiState.value = _uiState.value.copy(
                    resetState = null, resetCodeStep = false, resetToken = "", resetNewPassword = "", resetConfirmPassword = "",
                    submitState = Outcome.Success(Unit),
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(resetState = Outcome.Error(t.userMessageLogged("RoleLoginViewModel.confirmPasswordReset", ErrorClassifier.fallbackFor(step))))
            }
        }
    }
}
