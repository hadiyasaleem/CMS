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
import java.time.DayOfWeek
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

    val day = controller.day
    val shift = controller.shift
    val departments = controller.departments
    val sessions = controller.sessions
    val periods = controller.periods
    val loading = controller.loading
    val refreshError = controller.refreshError

    fun selectDay(day: DayOfWeek) = controller.selectDay(day)
    fun selectShift(shift: Session) = controller.selectShift(shift)
    fun refresh() = controller.refresh()
}

@Composable
fun MasterTimetableScreen(
    onOpenSession: (String) -> Unit,
    viewModel: MasterTimetableViewModel = hiltViewModel(),
) {
    val day by viewModel.day.collectAsState()
    val shift by viewModel.shift.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.refreshError.collectAsState()

    MasterTimetableWorkspace(
        day = day,
        shift = shift,
        departments = departments,
        sessions = sessions,
        periods = periods,
        loading = loading,
        errorMessage = error,
        onDayChange = viewModel::selectDay,
        onShiftChange = viewModel::selectShift,
        onRetry = viewModel::refresh,
        onOpenSession = onOpenSession,
    )
}
