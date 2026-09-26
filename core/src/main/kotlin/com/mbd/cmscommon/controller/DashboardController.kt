package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardController(
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    linkRequestRepository: StudentLinkRequestRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _filterScope = MutableStateFlow(ShiftScope.ALL)

    /** The Department -> Session -> Shift filter the counters follow. */
    val filterScope: StateFlow<ShiftScope> = _filterScope.asStateFlow()

    fun setFilterScope(scope: ShiftScope) {
        _filterScope.value = scope
    }

    val filterOptions: StateFlow<ScopeFilterOptions> =
        combine(departmentRepository.observeActiveDepartments(), sessionRepository.observeAllSessions()) { departments, sessions ->
            ScopeFilterOptions.of(departments, sessions)
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), ScopeFilterOptions())

    private val sources = combine(
        sessionRepository.observeActiveSessionStudentCount(),
        sessionRepository.observeAllStudentProfiles(),
        teacherRepository.observeActiveTeachers(),
        departmentRepository.observeActiveDepartments(),
        sessionRepository.observeAllSessions(),
    ) { studentCount, profiles, teachers, departments, sessions -> DashboardSources(studentCount, profiles, teachers, departments, sessions) }

    val state: StateFlow<DashboardState> = combine(
        sources,
        linkRequestRepository.observePendingRequests(),
        _filterScope,
    ) { sources, requests, filter ->
        dashboardState(sources, requests, filter)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), DashboardState())
}

data class DashboardSources(
    val activeStudentCount: Int,
    val profiles: List<StudentProfile>,
    val teachers: List<Teacher>,
    val departments: List<Department>,
    val sessions: List<AcademicSession>,
)

/**
 * The dashboard counters inside a scope. With nothing chosen they are college-wide. A department counts its own
 * teachers; a session or shift counts that intake's (or shift's) students and pending link requests.
 */
fun dashboardState(sources: DashboardSources, requests: List<StudentLinkRequest>, scope: ShiftScope): DashboardState {
    if (scope.isEmpty) {
        return DashboardState(
            students = sources.activeStudentCount,
            teachers = sources.teachers.size,
            departments = sources.departments.size,
            pendingRequests = requests.size,
            activeSessions = countActiveDashboardSessions(sources.sessions),
        )
    }
    val activeInScope = sources.sessions.filter { it.isActive && scope.matches(it) }
    val activeIds = activeInScope.map { it.sessionId }.toSet()
    return DashboardState(
        students = sources.profiles.count { it.sessionId in activeIds && scope.matches(deptOfSession(it.sessionId, sources.sessions), it.sessionId, it.shift) },
        teachers = sources.teachers.count { scope.deptId == null || it.deptId == scope.deptId },
        departments = if (scope.deptId != null) 1 else activeInScope.map { it.deptId }.distinct().size,
        pendingRequests = requests.inScope(scope, sources.sessions).size,
        activeSessions = activeInScope.size,
    )
}

fun countActiveDashboardSessions(sessions: List<AcademicSession>): Int =
    sessions.count { it.isActive }
