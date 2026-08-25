package com.mbd.cmsdesktop.ui.student

import androidx.compose.runtime.getValue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.EventsController
import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.CalendarWorkspace

/**
 * Read-only Events leaf for the student desktop app (mirrors mobile's `EventsScreen`) - students
 * can view events for their department/session but never create or delete them, so [EventsController]
 * (the read-only variant) is used instead of the manage-capable [com.mbd.cmscommon.controller.CalendarController].
 */
@Composable
fun StudentCalendarScreen(
    sessionId: String,
    departmentId: String,
    repository: CalendarRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository) { EventsController(repository, scope) }
    val events by controller.events.collectAsState()
    val loading by controller.loading.collectAsState()
    val busy by controller.busy.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()

    CalendarWorkspace(
        events = events.orEmpty(),
        viewer = CalendarViewerContext(CalendarViewerRole.STUDENT, departmentId, setOf(sessionId)),
        departments = emptyList(),
        sessions = emptyList(),
        canEdit = false,
        loading = loading,
        busy = busy,
        errorMessage = null,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onCreate = {},
        onDelete = {},
    )
}
