package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
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

    /** Null means "All classes": every student this teacher has, across every class, combined. */
    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    private val _assignments = MutableStateFlow<List<ResolvedAssignment>>(emptyList())

    /** The screen feeds in every class this teacher has, so the "All classes" roster can be built from it. */
    fun setAssignments(assignments: List<ResolvedAssignment>) {
        _assignments.value = assignments
    }

    /** One class per [ResolvedAssignment.classKey] -- a class taught under two subjects still counts once for a roster. */
    private val rosterClasses: StateFlow<List<ResolvedAssignment>> = combine(_selected, _assignments) { selected, all ->
        selected?.let { listOf(it) } ?: all.distinctBy { it.classKey }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The selected class's roster (merged-lecture aware), or -- with nothing selected -- every student across
     * every class this teacher has, deduplicated (the same student under two subjects of one class counts once). */
    val roster: StateFlow<List<SessionStudent>> = rosterClasses
        .flatMapLatest { classes ->
            if (classes.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(classes.map(::classRoster)) { lists -> lists.toList().flatten().distinctBy { it.id } }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun classRoster(assignment: ResolvedAssignment) =
        combine(assignment.sessionIds.map { sid -> sessionRepository.observeStudents(sid).map { studentsForTab(it, assignment.classShift) } }) {
            it.toList().flatten()
        }

    /** Every subject this teacher's attendance tally is built from (one entry per (session, course), not deduped
     * by class -- two subjects of the same class still tally separately). */
    private val tallyClasses: StateFlow<List<ResolvedAssignment>> = combine(_selected, _assignments) { selected, all ->
        selected?.let { listOf(it) } ?: all
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Keyed by SessionStudent.id ("${sessionId}_$rollNumber"), never bare roll numbers -- a merged class's two
    // sessions may otherwise reuse the same roll number and silently collide.
    val tallies: StateFlow<Map<String, AttendanceTally>> = tallyClasses
        .flatMapLatest { classes ->
            if (classes.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(classes.map(::classTallies)) { maps ->
                    // A student taught in more than one subject has more than one tally here; keep the lowest
                    // percentage -- the one worth flagging -- rather than whichever happened to load last.
                    maps.toList().flatMap { it.entries }.groupBy({ it.key }, { it.value })
                        .mapValues { (_, values) -> values.minBy { tally -> tally.percentage } }
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private fun classTallies(assignment: ResolvedAssignment) =
        combine(assignment.sessionIds.map { sid -> attendanceRepository.observeTallies(sid, assignment.courseCode).map { tallies -> sid to tallies } }) { pairs ->
            pairs.toList().flatMap { (sid, tallies) -> tallies.map { SessionStudent.buildId(sid, it.rollNumber) to it } }.toMap()
        }

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
    }

    /** Switches back to the combined "All classes" roster. */
    fun selectAll() {
        _selected.value = null
    }

    fun refresh() {
        val classes = _selected.value?.let { listOf(it) } ?: _assignments.value.distinctBy { it.classKey }
        if (classes.isEmpty()) return
        launch("refresh the class list") {
            val labelled = classes.flatMap { assignment ->
                assignment.sessionIds.flatMap { sid ->
                    listOf(
                        "the student list" to runCatching { sessionRepository.syncStudents(sid) },
                        "attendance summary" to runCatching { attendanceRepository.syncSummary(sid, assignment.courseCode) },
                    )
                }
            }
            val failures = FailureSummary.of(labelled)
            showError(FailureSummary.describe(failures, "MyStudentsController", prefix = "Couldn't refresh"))
        }
    }
}
