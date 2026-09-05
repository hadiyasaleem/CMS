package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DashboardController(
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    linkRequestRepository: StudentLinkRequestRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val state: StateFlow<DashboardState> = combine(
        sessionRepository.observeTotalStudentCount(),
        teacherRepository.observeActiveTeachers(),
        departmentRepository.observeActiveDepartments(),
        linkRequestRepository.observePendingRequests(),
        sessionRepository.observeAllSessions(),
    ) { studentCount, teachers, departments, requests, sessions ->
        DashboardState(
            students = studentCount,
            teachers = teachers.size,
            departments = departments.size,
            pendingRequests = requests.size,
            activeSessions = countActiveDashboardSessions(sessions),
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), DashboardState())
}

fun countActiveDashboardSessions(sessions: List<AcademicSession>): Int =
    sessions.count { it.isActive }
