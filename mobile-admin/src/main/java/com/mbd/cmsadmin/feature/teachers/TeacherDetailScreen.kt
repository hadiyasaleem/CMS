package com.mbd.cmsadmin.feature.teachers

import com.mbd.cmscommon.util.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.TeacherProfileController
import com.mbd.cmscommon.controller.TeacherScheduleController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherDetailWorkspace
import com.mbd.cmscommon.util.Outcome
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TeacherDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {
    private val teacherId: String = checkNotNull(savedStateHandle["teacherId"])

    private val profileController = TeacherProfileController(teacherId, teacherRepository, departmentRepository, timetableRepository, assignmentsProvider, viewModelScope)
    private val scheduleController = TeacherScheduleController(teacherId, departmentRepository, sessionRepository, timetableRepository, viewModelScope)

    val teacher = profileController.teacher
    val department = profileController.department
    val stats = profileController.stats
    val assignments = profileController.assignments
    val profileError = profileController.refreshError

    val sessions = scheduleController.sessions
    val grids = scheduleController.myGrids
    val scheduleOutcome = scheduleController.refreshState

    fun refresh() {
        profileController.refresh()
        scheduleController.refresh()
    }
}

/** Admin-only view of one teacher: profile, computed workload stats, and their own weekly schedule
 * grids -- the exact grid data [TeacherScheduleController] already builds for the teacher app itself,
 * just constructed here for an arbitrary teacherId instead of "whoever is logged in." */
@Composable
fun TeacherDetailScreen(viewModel: TeacherDetailViewModel = hiltViewModel()) {
    val teacher by viewModel.teacher.collectAsState()
    val department by viewModel.department.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val profileError by viewModel.profileError.collectAsState()

    val sessions by viewModel.sessions.collectAsState()
    val grids by viewModel.grids.collectAsState()
    val scheduleOutcome by viewModel.scheduleOutcome.collectAsState()

    TeacherDetailWorkspace(
        teacher = teacher,
        department = department,
        stats = stats,
        assignments = assignments,
        sessions = sessions,
        grids = grids,
        loading = scheduleOutcome is Outcome.Loading,
        errorMessage = profileError ?: (scheduleOutcome as? Outcome.Error)?.message,
        onRetry = viewModel::refresh,
        onExport = rememberDocumentExport(),
    )
}
