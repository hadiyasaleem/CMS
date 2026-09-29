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
            _uiState.value = _uiState.value.copy(resetState = outcome)
            onDone(outcome)
        }
    }
}
