package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.CalendarController
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.CalendarWorkspace

@Composable
fun CalendarScreen(
    repository: CalendarRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    createdBy: String?,
    viewer: CalendarViewerContext,
    canEdit: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, createdBy) {
        CalendarController(repository, createdBy.orEmpty(), scope)
    }
    val events by controller.events.collectAsState()
    val departments by departmentRepository.observeActiveDepartments().collectAsState(initial = emptyList())
    val sessions by sessionRepository.observeAllSessions().collectAsState(initial = emptyList())
    val loading by controller.loading.collectAsState()
    val busy by controller.busy.collectAsState()
    val errorMessage by controller.error.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()

    CalendarWorkspace(
        events = events.orEmpty(),
        viewer = viewer,
        departments = departments,
        sessions = sessions,
        canEdit = canEdit,
        loading = loading,
        busy = busy,
        errorMessage = errorMessage,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onCreate = controller::create,
        onDelete = controller::delete,
    )
}
