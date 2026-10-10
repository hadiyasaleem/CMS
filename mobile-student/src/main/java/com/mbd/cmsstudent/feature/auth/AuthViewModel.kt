package com.mbd.cmsstudent.feature.auth

import com.mbd.cmscommon.util.ErrorClassifier
import com.mbd.cmscommon.util.CmsException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.RegisterCooldownStore
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.StudentAuthUiState
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.userMessageLogged
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val registerCooldownStore: RegisterCooldownStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAuthUiState())
    val uiState: StateFlow<StudentAuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null, infoMessage = null, resetMessage = null, registerCooldownActive = false)
        viewModelScope.launch {
            val normalized = FieldValidators.normalizeEmail(value)
            if (registerCooldownStore.isOnCooldown(normalized) && _uiState.value.email == value) {
                _uiState.value = _uiState.value.copy(registerCooldownActive = true)
            }
        }
    }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, errorMessage = null) }
    fun onModeChange(registerMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            registerMode = registerMode, errorMessage = null, infoMessage = null, resetMessage = null, registerCooldownActive = false,
            resetCodeStep = false, resetToken = "", resetNewPassword = "", resetConfirmPassword = "",
        )
    }
    fun onResetTokenChange(value: String) { _uiState.value = _uiState.value.copy(resetToken = value, resetMessage = null) }
    fun onResetNewPasswordChange(value: String) { _uiState.value = _uiState.value.copy(resetNewPassword = value, resetMessage = null) }
    fun onResetConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(resetConfirmPassword = value, resetMessage = null) }
    fun cancelPasswordReset() {
        _uiState.value = _uiState.value.copy(resetCodeStep = false, resetToken = "", resetNewPassword = "", resetConfirmPassword = "", resetMessage = null)
    }

    fun submit() {
        val state = _uiState.value
        val email = FieldValidators.normalizeEmail(state.email)
        val validation = FieldValidators.emailError(state.email)
            ?: if (state.password.isEmpty()) "Password is required." else null
        if (validation != null) {
            _uiState.value = state.copy(errorMessage = validation)
            return
        }

        viewModelScope.launch {
            if (state.registerMode && registerCooldownStore.isOnCooldown(email)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "We already sent a verification link to $email. Check your inbox (and spam/junk folder), or try again in an hour.",
                    registerCooldownActive = true,
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, infoMessage = null)
            var step = if (state.registerMode) "create your account" else "sign in"
            try {
                if (state.registerMode) {
                    sessionManager.registerStudent(email, state.password)
                    step = "finish setting up your account"
                    val accountKey = sessionManager.accountKey
                    if (accountKey != null) {
                        // Email confirmation is disabled on this project -- a session exists immediately.
                        userRepository.provisionUnlinkedStudent(accountKey)
                        userRepository.touchLastLogin(accountKey)
                        _uiState.value = _uiState.value.copy(loading = false)
                    } else {
                        // Normal case: Supabase requires email confirmation before a session exists.
                        // AppRootViewModel's newlyAuthenticatedAccountKey collector finishes the
                        // account setup once the student opens the verification link on this device.
                        registerCooldownStore.recordAttempt(email)
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            infoMessage = "We sent a verification link to $email. Open it on this device to finish creating your account. " +
                                "Don't see it? Check your spam or junk folder too.",
                            registerCooldownActive = true,
                        )
                    }
                } else {
                    sessionManager.signIn(email, state.password)
                    step = "finish setting up your account"
                    val accountKey = sessionManager.accountKey
                        ?: throw CmsException.Auth("You signed in, but this account has no email address on record. Contact the college administrator.")
                    userRepository.provisionUnlinkedStudent(accountKey)
                    userRepository.touchLastLogin(accountKey)
                    _uiState.value = _uiState.value.copy(loading = false)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(loading = false, errorMessage = t.userMessageLogged("AuthViewModel.submit", ErrorClassifier.fallbackFor(step)))
            }
        }
    }

    fun sendPasswordReset() {
        val email = _uiState.value.email
        if (FieldValidators.emailError(email) != null) {
            _uiState.value = _uiState.value.copy(resetMessage = "Enter a valid email above first", resetError = true)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resetSending = true, resetMessage = null)
            try {
                sessionManager.sendPasswordReset(FieldValidators.normalizeEmail(email))
                _uiState.value = _uiState.value.copy(resetSending = false, resetCodeStep = true, resetMessage = null, resetError = false)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(resetSending = false, resetMessage = t.userMessageLogged("AuthViewModel.sendPasswordReset", "Couldn't send the password reset email to ${FieldValidators.normalizeEmail(email)}."), resetError = true)
            }
        }
    }

    fun confirmPasswordReset() {
        val state = _uiState.value
        val email = FieldValidators.normalizeEmail(state.email)
        val validation = FieldValidators.passwordConfirmationError(state.resetNewPassword, state.resetConfirmPassword)
            ?: FieldValidators.passwordError(state.resetNewPassword)
            ?: if (state.resetToken.isBlank()) "Enter the code we emailed you." else null
        if (validation != null) {
            _uiState.value = state.copy(resetMessage = validation, resetError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resetConfirming = true, resetMessage = null)
            try {
                sessionManager.confirmPasswordReset(email, state.resetToken, state.resetNewPassword)
                // AppRootViewModel's newlyAuthenticatedAccountKey collector finishes sign-in from
                // here, the same way it does for the email-verification deep link.
                _uiState.value = _uiState.value.copy(
                    resetConfirming = false, resetCodeStep = false, resetToken = "", resetNewPassword = "", resetConfirmPassword = "", resetMessage = null,
                )
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    resetConfirming = false,
                    resetMessage = t.userMessageLogged("AuthViewModel.confirmPasswordReset", "Couldn't reset your password. Check the code and try again."),
                    resetError = true,
                )
            }
        }
    }
}
