package com.mbd.cmsadmin.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val administratorRepository: AdministratorRepository,
) : ViewModel() {

    val accountKey: String = sessionManager.accountKey.orEmpty()

    val administrators: StateFlow<List<AdministratorAccount>> = administratorRepository.observeAdministrators()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val account: StateFlow<AdministratorAccount?> = administrators.map { accounts ->
        accounts.firstOrNull { it.email.equals(accountKey, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _loading.value = true
        _error.value = null
        runCatching { administratorRepository.sync() }
            .onFailure { _error.value = it.userMessage() }
        _loading.value = false
    }

    fun resetPassword() = viewModelScope.launch {
        _error.value = null
        _actionMessage.value = null
        runCatching { sessionManager.sendPasswordReset(accountKey) }
            .onSuccess { _actionMessage.value = "Password reset link sent to $accountKey." }
            .onFailure { _error.value = it.userMessage() }
    }

    fun signOut() = sessionManager.signOut()
}
