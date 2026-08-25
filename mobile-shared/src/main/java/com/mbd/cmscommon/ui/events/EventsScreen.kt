package com.mbd.cmscommon.ui.events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.ui.components.CalendarWorkspace

@Composable
fun EventsScreen(
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier,
) {
    val controller = viewModel.controller
    val events by controller.events.collectAsState()
    val loading by controller.loading.collectAsState()
    val busy by controller.busy.collectAsState()
    val error by controller.error.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val viewer by viewModel.resolvedViewer.collectAsState()

    val currentViewer = viewer
    if (currentViewer == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    CalendarWorkspace(
        events = events.orEmpty(),
        viewer = currentViewer,
        departments = departments,
        sessions = sessions,
        canEdit = currentViewer.role == CalendarViewerRole.ADMIN || currentViewer.role == CalendarViewerRole.TEACHER,
        loading = loading,
        busy = busy,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onCreate = { event -> controller.createEvent(event, viewModel.accountKey) },
        onDelete = controller::deleteEvent,
        modifier = modifier,
    )
}
