package com.mbd.cmsteacher.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmscommon.util.userMessageLogged
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {

    val accountKey: String get() = sessionManager.accountKey.orEmpty()

    val profile: StateFlow<Teacher?> = teacherRepository.observeTeacher(accountKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val assignments: StateFlow<List<ResolvedAssignment>> = assignmentsProvider.observeMyAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val departmentName: StateFlow<String?> = profile
        .map { it?.deptId }
        .distinctUntilChanged()
        .flatMapLatest { deptId ->
            if (deptId.isNullOrBlank()) {
                flowOf(null)
            } else {
                flowOf(runCatching { departmentRepository.getDepartment(deptId) }.orLogCritical("TeacherProfileViewModel.getDepartment")?.name)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun resetPassword() {
        val email = sessionManager.accountKey ?: return
        viewModelScope.launch {
            try {
                sessionManager.sendPasswordReset(email)
                _actionMessage.value = "Password reset email sent."
            } catch (t: Throwable) {
                _error.value = t.userMessageLogged("TeacherProfileViewModel.resetPassword", "Could not send the reset email.")
            }
        }
    }

    fun signOut() {
        sessionManager.signOut()
    }
}
