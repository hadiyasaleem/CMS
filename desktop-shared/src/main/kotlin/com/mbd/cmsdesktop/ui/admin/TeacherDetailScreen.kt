package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.TeacherProfileController
import com.mbd.cmscommon.controller.TeacherScheduleController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDetailWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmsdesktop.platform.rememberDocumentExport

/** Admin-only view of one teacher: profile, computed workload stats, and their own weekly schedule
 * grids -- the exact grid data [TeacherScheduleController] already builds for the teacher app itself,
 * just constructed here for an arbitrary [teacherId] instead of "whoever is logged in." */
@Composable
fun TeacherDetailScreen(
    teacherId: String,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) {
    val scope = rememberCoroutineScope()
    val profileController = remember(teacherId) {
        TeacherProfileController(teacherId, teacherRepository, departmentRepository, timetableRepository, assignmentsProvider, scope)
    }
    val scheduleController = remember(teacherId) {
        TeacherScheduleController(teacherId, departmentRepository, sessionRepository, timetableRepository, scope)
    }

    val teacher by profileController.teacher.collectAsState()
    val department by profileController.department.collectAsState()
    val stats by profileController.stats.collectAsState()
    val assignments by profileController.assignments.collectAsState()
    val profileError by profileController.refreshError.collectAsState()

    val sessions by scheduleController.sessions.collectAsState()
    val grids by scheduleController.myGrids.collectAsState()
    val scheduleOutcome by scheduleController.refreshState.collectAsState()

    TeacherDetailWorkspace(
        teacher = teacher,
        department = department,
        stats = stats,
        assignments = assignments,
        sessions = sessions,
        grids = grids,
        loading = scheduleOutcome is Outcome.Loading,
        errorMessage = profileError ?: (scheduleOutcome as? Outcome.Error)?.message,
        onRetry = { profileController.refresh(); scheduleController.refresh() },
        onExport = rememberDocumentExport(),
    )
}
