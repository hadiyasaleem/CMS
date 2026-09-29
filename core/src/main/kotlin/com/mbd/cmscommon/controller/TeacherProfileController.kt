package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.util.userMessageLogged
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The admin-facing "one teacher" view: their profile, their classes, and their computed weekly
 * workload -- the schedule grid itself is a plain [TeacherScheduleController] built alongside this
 * one at the screen level (it's already reusable for any teacherId, not just "self").
 */
class TeacherProfileController(
    private val teacherId: String,
    private val teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val teacher: StateFlow<Teacher?> =
        teacherRepository.observeTeacher(teacherId).map<Teacher, Teacher?> { it }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val department: StateFlow<Department?> = combine(teacher, departments) { t, depts ->
        t?.deptId?.let { id -> depts.firstOrNull { it.deptId == id } }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeMyPeriods(teacherId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<ResolvedAssignment>> =
        assignmentsProvider.observeAssignmentsFor(teacherId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<TeacherWorkloadStats> = combine(periods, assignments) { p, a -> computeTeacherWorkloadStats(p, a) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), computeTeacherWorkloadStats(emptyList(), emptyList()))

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    fun refresh() = launch("refresh this teacher's profile") {
        _refreshError.value = runCatching { teacherRepository.sync() }
            .exceptionOrNull()?.userMessageLogged("TeacherProfileController.refresh", "Could not refresh this teacher's profile.")
    }
}
