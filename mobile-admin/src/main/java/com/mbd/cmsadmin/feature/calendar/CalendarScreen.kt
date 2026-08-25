package com.mbd.cmsadmin.feature.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.CalendarController
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.CalendarWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    calendarRepository: CalendarRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    val controller = CalendarController(calendarRepository, sessionManager.accountKey.orEmpty(), viewModelScope)

    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val events by viewModel.controller.events.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val loading by viewModel.controller.loading.collectAsState()
    val busy by viewModel.controller.busy.collectAsState()
    val error by viewModel.controller.error.collectAsState()
    val actionMessage by viewModel.controller.actionMessage.collectAsState()

    CalendarWorkspace(
        events = events.orEmpty(),
        viewer = CalendarViewerContext(CalendarViewerRole.ADMIN),
        departments = departments,
        sessions = sessions,
        canEdit = true,
        loading = loading,
        busy = busy,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = viewModel.controller::refresh,
        onCreate = viewModel.controller::create,
        onDelete = viewModel.controller::delete,
    )
}
