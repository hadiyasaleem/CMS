package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.MasterTimetableController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.MasterTimetableWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MasterTimetableViewModel @Inject constructor(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
) : ViewModel() {
    private val controller = MasterTimetableController(
        departmentRepository,
        sessionRepository,
        timetableRepository,
        viewModelScope,
    )

    val departments = controller.departments
    val sessions = controller.sessions
    val sessionsInDepartment = controller.sessionsInDepartment
    val shiftsForSelection = controller.shiftsForSelection
    val selectedDeptId = controller.selectedDeptId
    val selectedStartYear = controller.selectedStartYear
    val selectedShift = controller.selectedShift
    val resolvedSession = controller.resolvedSession
    val periods = controller.periods
    val loading = controller.loading
    val refreshError = controller.refreshError

    fun selectDepartment(deptId: String?) = controller.selectDepartment(deptId)
    fun selectStartYear(year: Int?) = controller.selectStartYear(year)
    fun selectShift(shift: Session?) = controller.selectShift(shift)
    fun refresh() = controller.refresh()
}

@Composable
fun MasterTimetableScreen(
    onOpenSession: (String) -> Unit,
    viewModel: MasterTimetableViewModel = hiltViewModel(),
) {
    val departments by viewModel.departments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val sessionsInDepartment by viewModel.sessionsInDepartment.collectAsState()
    val shiftsForSelection by viewModel.shiftsForSelection.collectAsState()
    val selectedDeptId by viewModel.selectedDeptId.collectAsState()
    val selectedStartYear by viewModel.selectedStartYear.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()
    val resolvedSession by viewModel.resolvedSession.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.refreshError.collectAsState()

    MasterTimetableWorkspace(
        departments = departments,
        sessions = sessions,
        sessionsInDepartment = sessionsInDepartment,
        shiftsForSelection = shiftsForSelection,
        selectedDeptId = selectedDeptId,
        selectedStartYear = selectedStartYear,
        selectedShift = selectedShift,
        resolvedSession = resolvedSession,
        periods = periods,
        loading = loading,
        errorMessage = error,
        onSelectDepartment = viewModel::selectDepartment,
        onSelectStartYear = viewModel::selectStartYear,
        onSelectShift = viewModel::selectShift,
        onRetry = viewModel::refresh,
        onOpenSession = onOpenSession,
    )
}
