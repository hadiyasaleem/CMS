package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.orLogCritical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    /** The combined roster of every session sharing this lecture, for a merged class. */
    val roster: StateFlow<List<SessionStudent>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyList())
            } else {
                combine(assignment.sessionIds.map { sid -> sessionRepository.observeStudents(sid).map { studentsForTab(it, assignment.classShift) } }) {
                    it.toList().flatten()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Keyed by SessionStudent.id ("${sessionId}_$rollNumber"), never bare roll numbers -- a merged class's two
    // sessions may otherwise reuse the same roll number and silently collide.
    val tallies: StateFlow<Map<String, AttendanceTally>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyMap())
            } else {
                combine(assignment.sessionIds.map { sid -> attendanceRepository.observeTallies(sid, assignment.courseCode).map { tallies -> sid to tallies } }) { pairs ->
                    pairs.toList().flatMap { (sid, tallies) -> tallies.map { SessionStudent.buildId(sid, it.rollNumber) to it } }.toMap()
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
    }

    fun refresh() {
        val assignment = _selected.value ?: return
        launch("refresh the class list") {
            val labelled = assignment.sessionIds.flatMap { sid ->
                listOf(
                    "the student list" to runCatching { sessionRepository.syncStudents(sid) },
                    "attendance summary" to runCatching { attendanceRepository.syncSummary(sid, assignment.courseCode) },
                )
            }
            val failures = FailureSummary.of(labelled)
            showError(FailureSummary.describe(failures, "MyStudentsController", prefix = "Couldn't refresh"))
        }
    }
}
