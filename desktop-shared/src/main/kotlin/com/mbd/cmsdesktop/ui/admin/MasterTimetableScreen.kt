package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.MasterTimetableController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.MasterTimetableWorkspace

@Composable
fun MasterTimetableScreen(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
    onOpenSession: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(departmentRepository, sessionRepository, timetableRepository) {
        MasterTimetableController(departmentRepository, sessionRepository, timetableRepository, scope)
    }
    val departments by controller.departments.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val sessionsInDepartment by controller.sessionsInDepartment.collectAsState()
    val shiftsForSelection by controller.shiftsForSelection.collectAsState()
    val selectedDeptId by controller.selectedDeptId.collectAsState()
    val selectedStartYear by controller.selectedStartYear.collectAsState()
    val selectedShift by controller.selectedShift.collectAsState()
    val resolvedSession by controller.resolvedSession.collectAsState()
    val periods by controller.periods.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.refreshError.collectAsState()

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
        errorMessage = errorMessage,
        onSelectDepartment = controller::selectDepartment,
        onSelectStartYear = controller::selectStartYear,
        onSelectShift = controller::selectShift,
        onRetry = controller::refresh,
        onOpenSession = onOpenSession,
    )
}
