package com.mbd.cmsstudent.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.components.StudentAuthUiState
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.userMessage
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAuthUiState())
    val uiState: StateFlow<StudentAuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, errorMessage = null, infoMessage = null, resetMessage = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, errorMessage = null) }
    fun onModeChange(registerMode: Boolean) {
        _uiState.value = _uiState.value.copy(registerMode = registerMode, errorMessage = null, infoMessage = null, resetMessage = null)
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
            _uiState.value = _uiState.value.copy(loading = true, errorMessage = null, infoMessage = null)
            try {
                if (state.registerMode) {
                    sessionManager.registerStudent(email, state.password)
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
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            infoMessage = "We sent a verification link to $email. Open it on this device to finish creating your account.",
                        )
                    }
                } else {
                    sessionManager.signIn(email, state.password)
                    val accountKey = sessionManager.accountKey ?: error("Signed in but no email on account")
                    userRepository.provisionUnlinkedStudent(accountKey)
                    userRepository.touchLastLogin(accountKey)
                    _uiState.value = _uiState.value.copy(loading = false)
                }
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(loading = false, errorMessage = t.userMessage("Sign-in failed. Please try again."))
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
                _uiState.value = _uiState.value.copy(resetSending = false, resetMessage = "Password reset email sent.", resetError = false)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(resetSending = false, resetMessage = t.userMessage("Could not send the reset email."), resetError = true)
            }
        }
    }
}
