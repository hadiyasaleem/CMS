package com.mbd.cmsadmin.feature.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper
import com.mbd.cmscommon.data.sync.StartupBootstrapTracker
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val adminDataBootstrapper: AdminDataBootstrapper,
    private val startupBootstrapTracker: StartupBootstrapTracker,
) : ViewModel() {

    private val startupRole = MutableStateFlow<UserRole?>(null)

    val role: StateFlow<UserRole?> = startupRole.combine(userRepository.observeCurrentUserRole()) { startup, observed ->
        observed ?: startup
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** True once the splash's login check (Room role resolution) has settled — drives the splash gate. */
    private val _authChecked = MutableStateFlow(false)
    val authChecked: StateFlow<Boolean> = _authChecked.asStateFlow()

    private val _readyAccount = MutableStateFlow<String?>(null)
    val readyAccount: StateFlow<String?> = _readyAccount.asStateFlow()

    private val _isBootstrapping = MutableStateFlow(false)
    val isBootstrapping: StateFlow<Boolean> = _isBootstrapping.asStateFlow()

    private val bootstrapMutex = Mutex()

    init {
        // Warm the Room-cached role from Firestore right after process start / sign-in, so
        // observeCurrentUserRole() has something to emit before the first manual refresh, then
        // pull the admin reference data (depts/terms/classes/subjects/offerings/assignments/
        // teachers/link-requests) into Room. Best-effort only: offline-first means a failure
        // here (e.g. no network at cold start) must never crash the app — Room's last-cached
        // data still drives the UI, and the user can retry via a manual refresh.
        viewModelScope.launch {
            val accountKey = sessionManager.awaitInitialization()
            if (accountKey == null) {
                startupRole.value = null
                runCatching { userRepository.clearLocalCache() }
                _authChecked.value = true
                return@launch
            }

            val cachedRole = runCatching { userRepository.getCachedRole(accountKey) }.getOrNull() as? UserRole.Admin
            startupRole.value = cachedRole
            if (cachedRole != null) {
                ensureAdminData(accountKey)
                _authChecked.value = true
            }

            // Each step isolated so one failure never skips the reference-data pull.
            val resolved = runCatching { userRepository.resolveRole(accountKey) }.getOrNull() as? UserRole.Admin
            val effectiveRole = resolved ?: cachedRole
            if (effectiveRole != null) {
                ensureAdminData(accountKey)
            } else {
                _readyAccount.value = accountKey
            }
                _authChecked.value = true
        }

        viewModelScope.launch {
            userRepository.observeCurrentUserRole().distinctUntilChanged().collectLatest { resolved ->
                if (resolved is UserRole.Admin) ensureAdminData(resolved.uid)
            }
        }
    }

    private suspend fun ensureAdminData(accountKey: String) {
        bootstrapMutex.withLock {
            if (_readyAccount.value == accountKey) return
            _isBootstrapping.value = true
            try {
                val completed = runCatching { adminDataBootstrapper.refreshAll() }.getOrDefault(false)
                if (completed) {
                    runCatching {
                        startupBootstrapTracker.markComplete(StartupBootstrapTracker.ADMIN_DATA, accountKey)
                    }
                }
                _readyAccount.value = accountKey
            } finally {
                _isBootstrapping.value = false
            }
        }
    }
    fun signOut() {
        startupRole.value = null
        _readyAccount.value = null
        sessionManager.signOut()
        viewModelScope.launch { userRepository.clearLocalCache() }
    }
}
