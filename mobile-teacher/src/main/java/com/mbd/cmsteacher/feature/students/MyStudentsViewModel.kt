package com.mbd.cmsteacher.feature.students

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.controller.studentsForTab
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyStudentsViewModel @Inject constructor(
    assignmentsProvider: TeacherAssignmentsProvider,
    private val sessionRepository: AcademicSessionRepository,
    private val attendanceRepository: SessionAttendanceRepository,
) : ViewModel() {

    val assignments: StateFlow<List<ResolvedAssignment>> = assignmentsProvider.observeMyAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    /** Null means "All classes": every student this teacher has, across every class, combined. */
    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    /** One class per [ResolvedAssignment.classKey] -- a class taught under two subjects still counts once for a roster. */
    private val rosterClasses: StateFlow<List<ResolvedAssignment>> = combine(_selected, assignments) { selected, all ->
        selected?.let { listOf(it) } ?: all.distinctBy { it.classKey }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val students: StateFlow<List<SessionStudent>> = rosterClasses
        .flatMapLatest { classes ->
            if (classes.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(classes.map { a -> sessionRepository.observeStudents(a.sessionId).map { studentsForTab(it, a.classShift) } }) { lists ->
                    lists.toList().flatten().distinctBy { it.id }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Every subject this teacher's attendance tally is built from (one entry per class+subject, not deduped by
     * class -- two subjects of the same class still tally separately). */
    private val tallyClasses: StateFlow<List<ResolvedAssignment>> = combine(_selected, assignments) { selected, all ->
        selected?.let { listOf(it) } ?: all
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tallies: StateFlow<Map<String, AttendanceTally>> = tallyClasses
        .flatMapLatest { classes ->
            if (classes.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(classes.map { a -> attendanceRepository.observeTallies(a.sessionId, a.courseCode).map { t -> t.associateBy { SessionStudent.buildId(a.sessionId, it.rollNumber) } } }) { maps ->
                    // A student taught in more than one subject has more than one tally here; keep the lowest
                    // percentage -- the one worth flagging -- rather than whichever happened to load last.
                    maps.toList().flatMap { it.entries }.groupBy({ it.key }, { it.value })
                        .mapValues { (_, values) -> values.minBy { tally -> tally.percentage } }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun selectAssignment(assignment: ResolvedAssignment) {
        _selected.value = assignment
        refreshSelection(listOf(assignment))
    }

    /** Switches back to the combined "All classes" roster and refreshes every class at once. */
    fun selectAll() {
        _selected.value = null
        refreshSelection(assignments.value.distinctBy { it.classKey })
    }

    private fun refreshSelection(classes: List<ResolvedAssignment>) {
        _syncError.value = null
        if (classes.isEmpty()) return
        viewModelScope.launch {
            val failures = FailureSummary.of(
                classes.flatMap { assignment ->
                    listOf(
                        "the student list" to runCatching { sessionRepository.syncStudents(assignment.sessionId) },
                        "attendance summary" to runCatching { attendanceRepository.syncSummary(assignment.sessionId, assignment.courseCode) },
                    )
                },
            )
            val stillCurrent = if (classes.size == 1) _selected.value == classes.single() else _selected.value == null
            if (stillCurrent) _syncError.value = FailureSummary.describe(failures, "MyStudentsViewModel", prefix = "Couldn't refresh")
        }
    }
}
