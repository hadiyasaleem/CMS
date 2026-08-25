package com.mbd.cmscommon.ui.datesheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.ui.components.DatesheetWorkspace

@Composable
fun DatesheetsScreen(
    viewModel: DatesheetsViewModel,
    modifier: Modifier = Modifier,
) {
    val controller = viewModel.controller
    val datesheets by controller.sheets.collectAsState()
    val slots by controller.slots.collectAsState()
    val loadingSlots by controller.loadingSlots.collectAsState()
    val refreshing by controller.refreshing.collectAsState()
    val busy by controller.busy.collectAsState()
    val error by controller.error.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val subjectsBySession by viewModel.subjectsBySession.collectAsState()
    val invigilators by viewModel.invigilators.collectAsState()
    val viewer by viewModel.viewer.collectAsState()

    DatesheetWorkspace(
        datesheets = datesheets.orEmpty(),
        slots = slots,
        loadingSlots = loadingSlots,
        sessions = sessions,
        subjectsBySession = subjectsBySession,
        invigilators = invigilators,
        viewer = viewer,
        loading = datesheets == null || refreshing,
        busy = busy,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onLoadSlots = { id -> controller.loadSlots(id) },
        onLoadSubjects = viewModel::loadSubjects,
        onCreate = controller::createDatesheet,
        onUpdate = controller::updateDatesheet,
        onSetPublished = controller::setPublished,
        onDelete = controller::deleteDatesheet,
        onAddSlot = controller::addSlot,
        onUpdateSlot = controller::updateSlot,
        onDeleteSlot = controller::deleteSlot,
        modifier = modifier,
    )
}
