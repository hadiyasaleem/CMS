package com.mbd.cmsteacher.feature.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.sync.StartupBootstrapTracker
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val teacherRepository: TeacherRepository,
    private val dataBootstrapper: AdminDataBootstrapper,
    private val startupBootstrapTracker: StartupBootstrapTracker,
) : ViewModel() {

    private val startupRole = MutableStateFlow<UserRole?>(null)

    val role: StateFlow<UserRole?> = startupRole.combine(userRepository.observeCurrentUserRole()) { startup, observed ->
        observed ?: startup
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _authChecked = MutableStateFlow(false)
    val authChecked: StateFlow<Boolean> = _authChecked.asStateFlow()

    init {
        viewModelScope.launch {
            val accountKey = sessionManager.awaitInitialization()
            if (accountKey == null) {
                startupRole.value = null
                _authChecked.value = true
                return@launch
            }

            val cachedRole = runCatching { userRepository.getCachedRole(accountKey) }.getOrNull() as? UserRole.Teacher
            startupRole.value = cachedRole
            if (cachedRole != null) _authChecked.value = true

            runCatching { userRepository.resolveRole(accountKey) }
            runCatching { teacherRepository.syncSelf(accountKey) }

            val completed = runCatching { dataBootstrapper.refreshAll() }.getOrDefault(false)
            if (completed) {
                runCatching { startupBootstrapTracker.markComplete(StartupBootstrapTracker.REFERENCE_DATA, accountKey) }
            }
            _authChecked.value = true
        }
    }

    fun signOut() {
        startupRole.value = null
        sessionManager.signOut()
        viewModelScope.launch { userRepository.clearLocalCache() }
    }
}
