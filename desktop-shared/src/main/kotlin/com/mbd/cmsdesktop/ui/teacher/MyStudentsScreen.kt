package com.mbd.cmsdesktop.ui.teacher

import com.mbd.cmsdesktop.platform.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.controller.MyStudentsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherStudentRosterWorkspace

/**
 * Roster tab reachable from the menu hub / home quick actions. [timetableRepository] is accepted
 * to match the decompiled call site (the shell resolves it before invoking this screen) even
 * though the controller itself only needs session + attendance repositories.
 */
@Composable
fun MyStudentsScreen(
    teacherId: String,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    @Suppress("UNUSED_PARAMETER") timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionRepository, attendanceRepository) {
        MyStudentsController(sessionRepository, attendanceRepository, scope)
    }
    val selected by controller.selected.collectAsState()
    val roster by controller.roster.collectAsState()
    val tallies by controller.tallies.collectAsState()
    val syncError by controller.error.collectAsState()
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
    var didInitialRefresh by remember { mutableStateOf(false) }

    LaunchedEffect(assignments, selected) {
        controller.setAssignments(assignments)
        // A class that was picked but is no longer taught (dropped from the timetable) falls back to "All classes"
        // rather than silently picking a different one for the teacher.
        val selectionExists = selected == null || assignments.any { it.classKey == selected?.classKey && it.courseCode == selected?.courseCode }
        if (!selectionExists) controller.selectAll()
        // One combined refresh across every class, the first time this teacher's classes are known.
        if (!didInitialRefresh && assignments.isNotEmpty()) {
            didInitialRefresh = true
            controller.refresh()
        }
    }

    TeacherStudentRosterWorkspace(

        onExport = rememberDocumentExport(),
        assignments = assignments,
        selected = selected,
        students = roster,
        tallies = tallies,
        onSelectAssignment = controller::select,
        onShowAllClasses = controller::selectAll,
        syncError = syncError,
    )
}
