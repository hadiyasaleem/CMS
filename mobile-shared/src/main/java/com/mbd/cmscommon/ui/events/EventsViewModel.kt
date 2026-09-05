package com.mbd.cmscommon.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.EventsController
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.util.StudentIdCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class EventsViewModel @Inject constructor(
    calendarRepository: CalendarRepository,
    userRepository: UserRepository,
    teacherRepository: TeacherRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    sessionManager: SessionManager,
) : ViewModel() {

    val controller = EventsController(calendarRepository, viewModelScope)

    val accountKey: String = sessionManager.accountKey ?: ""

    val resolvedViewer: StateFlow<CalendarViewerContext?> = combine(
        userRepository.observeCurrentUserRole(),
        teacherRepository.observeTeacher(accountKey),
        assignmentsProvider.observeMyAssignments(),
    ) { role, teacher, teaching ->
        when (role) {
            null -> null
            is UserRole.Admin -> CalendarViewerContext(CalendarViewerRole.ADMIN)
            is UserRole.Teacher -> CalendarViewerContext(
                CalendarViewerRole.TEACHER,
                teacher?.deptId,
                teaching.map { it.sessionId }.toSet(),
            )
            is UserRole.LinkedStudent -> {
                val sessionId = StudentIdCodec.sessionIdOf(role.studentId)
                CalendarViewerContext(CalendarViewerRole.STUDENT, StudentIdCodec.deptIdOf(sessionId), setOf(sessionId))
            }
            is UserRole.UnlinkedStudent -> CalendarViewerContext(CalendarViewerRole.STUDENT)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
