package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())

    LaunchedEffect(assignments, selected) {
        val selectionExists = selected != null &&
            assignments.any { it.sessionId == selected?.sessionId && it.courseCode == selected?.courseCode }
        if (!selectionExists) {
            assignments.firstOrNull()?.let { controller.select(it) }
        }
    }

    TeacherStudentRosterWorkspace(
        assignments = assignments,
        selected = selected,
        students = roster,
        tallies = tallies,
        onSelectAssignment = controller::select,
    )
}
