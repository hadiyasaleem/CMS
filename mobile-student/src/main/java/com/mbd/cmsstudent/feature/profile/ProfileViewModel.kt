package com.mbd.cmsstudent.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.util.userMessage
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import com.mbd.cmsstudent.feature.common.StudentContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudentProfileScreenState(
    val context: StudentContext,
    val department: Department?,
    val profile: StudentProfile?,
    val fines: List<Fine>,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    currentStudentProvider: CurrentStudentProvider,
    private val sessionRepository: AcademicSessionRepository,
    private val departmentRepository: DepartmentRepository,
    private val fineRepository: FineRepository,
) : ViewModel() {

    val accountKey: String get() = sessionManager.accountKey.orEmpty()

    private val _refreshTrigger = MutableStateFlow(0)
    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val state: StateFlow<StudentProfileScreenState?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                flowOf<StudentProfileScreenState?>(null)
            } else {
                _refreshTrigger.map {
                    val department = runCatching { departmentRepository.getDepartment(context.deptId) }.getOrNull()
                    val profile = runCatching { sessionRepository.getStudentProfile(context.sessionId, context.rollNumber) }.getOrNull()
                    val fines = runCatching { fineRepository.getFines(context.sessionId, context.rollNumber) }.getOrDefault(emptyList())
                    StudentProfileScreenState(context, department, profile, fines)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }

    fun resetPassword() {
        val email = sessionManager.accountKey ?: return
        viewModelScope.launch {
            try {
                sessionManager.sendPasswordReset(email)
                _actionMessage.value = "Password reset email sent."
            } catch (t: Throwable) {
                _error.value = t.userMessage("Could not send the reset email.")
            }
        }
    }

    fun signOut() {
        sessionManager.signOut()
    }
}
