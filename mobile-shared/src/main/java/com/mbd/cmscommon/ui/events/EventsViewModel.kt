package com.mbd.cmscommon.ui.events

import com.mbd.cmscommon.controller.observeShiftOf
import com.mbd.cmscommon.controller.taughtClasses
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    ) { role, teacher, teaching -> Triple(role, teacher, teaching) }.flatMapLatest { (role, teacher, teaching) ->
        when (role) {
            null -> flowOf(null)
            is UserRole.Admin -> flowOf(CalendarViewerContext(CalendarViewerRole.ADMIN))
            // A teacher sees events for their department and the sessions/shifts they teach.
            is UserRole.Teacher -> flowOf(
                CalendarViewerContext(
                    CalendarViewerRole.TEACHER,
                    teacher?.deptId,
                    teaching.map { it.sessionId }.toSet(),
                    taughtClasses = teaching.taughtClasses(),
                ),
            )
            // A student sees their department, session and own shift's events.
            is UserRole.LinkedStudent -> {
                val sessionId = StudentIdCodec.sessionIdOf(role.studentId)
                sessionRepository.observeShiftOf(sessionId, StudentIdCodec.rollOf(role.studentId)).map { shift ->
                    CalendarViewerContext(CalendarViewerRole.STUDENT, StudentIdCodec.deptIdOf(sessionId), setOf(sessionId), shift)
                }
            }
            is UserRole.UnlinkedStudent -> flowOf(CalendarViewerContext(CalendarViewerRole.STUDENT))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
