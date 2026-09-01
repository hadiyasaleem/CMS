package com.mbd.cmsteacher.feature.students

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

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    val students: StateFlow<List<SessionStudent>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) flowOf(emptyList()) else sessionRepository.observeStudents(assignment.sessionId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tallies: StateFlow<Map<String, AttendanceTally>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) {
                flowOf(emptyMap())
            } else {
                attendanceRepository.observeTallies(assignment.sessionId, assignment.courseCode)
                    .map { list -> list.associateBy { it.rollNumber } }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun selectAssignment(assignment: ResolvedAssignment) {
        _selected.value = assignment
        // observeStudents/observeTallies are local-cache flows; pull remote data on selection
        // (mirrors MyStudentsController.select) so the roster/tallies populate on a cold cache.
        viewModelScope.launch {
            runCatching { sessionRepository.syncStudents(assignment.sessionId) }
            runCatching { attendanceRepository.syncSummary(assignment.sessionId, assignment.courseCode) }
        }
    }
}
