package com.mbd.cmsadmin.feature.departments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DepartmentPortfolioStats
import com.mbd.cmscommon.controller.DepartmentsActionController
import com.mbd.cmscommon.controller.departmentPortfolioStats
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.state.SyncedListState
import com.mbd.cmscommon.ui.state.syncedListState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DepartmentsViewModel @Inject constructor(
    repository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val teachers = teacherRepository.observeActiveTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val listState: SyncedListState<Department> = syncedListState(
        roomFlow = repository.observeActiveDepartments(),
        onRefresh = {
            repository.sync()
            repository.observeActiveDepartments().first().forEach { department ->
                sessionRepository.syncSessionsForDept(department.deptId)
            }
            sessionRepository.observeAllSessions().first().forEach { session ->
                sessionRepository.syncStudents(session.sessionId)
            }
        },
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val departmentStats = sessionRepository.observeAllSessions()
        .flatMapLatest { sessions ->
            if (sessions.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(sessions.map { session ->
                    sessionRepository.observeStudents(session.sessionId).map { students ->
                        session to students.size
                    }
                }) { sessionCounts ->
                    departmentPortfolioStats(sessionCounts.toList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun refresh() = listState.refresh(viewModelScope)

    private val actionController = DepartmentsActionController(
        repo = repository,
        createdBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    fun createDepartment(name: String, code: String, hodEmail: String? = null, description: String? = null) =
        actionController.create(name, code, hodEmail, description)
    fun updateDepartment(existing: Department, name: String, code: String, hodEmail: String?, description: String?) =
        actionController.update(existing, name, code, hodEmail, description)
    fun delete(deptId: String) = actionController.delete(deptId)
    val actionError = actionController.error
    fun clearActionError() = actionController.clearError()
}
