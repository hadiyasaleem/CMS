package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MyStudentsController(
    private val sessionRepository: AcademicSessionRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    val roster: StateFlow<List<SessionStudent>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) flowOf(emptyList()) else sessionRepository.observeStudents(assignment.sessionId)
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tallies: StateFlow<Map<String, AttendanceTally>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyMap())
            } else {
                attendanceRepository.observeTallies(assignment.sessionId, assignment.courseCode)
                    .map { list -> list.associateBy { it.rollNumber } }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
    }

    fun refresh() {
        val assignment = _selected.value ?: return
        launch {
            runCatching { sessionRepository.syncStudents(assignment.sessionId) }
            runCatching { attendanceRepository.syncSummary(assignment.sessionId, assignment.courseCode) }
        }
    }
}
