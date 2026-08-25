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
    val day by controller.day.collectAsState()
    val shift by controller.shift.collectAsState()
    val departments by controller.departments.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val periods by controller.periods.collectAsState()
    val loading by controller.loading.collectAsState()
    val errorMessage by controller.refreshError.collectAsState()

    MasterTimetableWorkspace(
        day = day,
        shift = shift,
        departments = departments,
        sessions = sessions,
        periods = periods,
        loading = loading,
        errorMessage = errorMessage,
        onDayChange = controller::selectDay,
        onShiftChange = controller::selectShift,
        onRetry = controller::refresh,
        onOpenSession = onOpenSession,
    )
}
