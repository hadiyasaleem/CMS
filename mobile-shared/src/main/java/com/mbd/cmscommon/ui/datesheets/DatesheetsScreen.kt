package com.mbd.cmscommon.ui.datesheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.controller.DatesheetEditorController
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.ui.components.DatesheetDetailData
import com.mbd.cmscommon.ui.components.DatesheetWorkspace

@Composable
fun DatesheetsScreen(
    viewModel: DatesheetsViewModel,
    modifier: Modifier = Modifier,
) {
    val controller = viewModel.browseController
    val departments by controller.departments.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val datesheets by controller.datesheets.collectAsState()
    val selectedDeptId by controller.selectedDeptId.collectAsState()
    val selectedStartYear by controller.selectedStartYear.collectAsState()
    val selectedShift by controller.selectedShift.collectAsState()
    val selectedSemester by controller.selectedSemester.collectAsState()
    val sessionsInDepartment by controller.sessionsInDepartment.collectAsState()
    val shiftsForSelection by controller.shiftsForSelection.collectAsState()
    val resolvedSession by controller.resolvedSession.collectAsState()
    val browseError by controller.error.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val viewer by viewModel.viewer.collectAsState()
    val openDatesheetId by viewModel.openDatesheetId.collectAsState()
    val editorController by viewModel.editorController.collectAsState()

    val detail: DatesheetDetailData? = editorController?.let { collectDatesheetDetail(it, departments) }
    val detailBusy = editorController?.let { collectBusy(it) } ?: false
    val detailError = editorController?.error?.collectAsState()?.value

    DatesheetWorkspace(
        viewer = viewer,
        departments = departments,
        sessions = sessions,
        datesheets = datesheets,
        allSlots = allSlots,
        selectedDeptId = selectedDeptId,
        selectedStartYear = selectedStartYear,
        selectedShift = selectedShift,
        selectedSemester = selectedSemester,
        sessionsInDepartment = sessionsInDepartment,
        shiftsForSelection = shiftsForSelection,
        resolvedSession = resolvedSession,
        onSelectDepartment = controller::selectDepartment,
        onSelectStartYear = controller::selectStartYear,
        onSelectShift = controller::selectShift,
        onSelectSemester = controller::selectSemester,
        buildings = buildings,
        loading = false,
        errorMessage = browseError ?: detailError,
        onRetry = { controller.refresh() },
        onCreateDatesheet = { defaultStart, defaultEnd, defaultBuildingId, instructions ->
            val session = resolvedSession
            val semester = selectedSemester
            if (session != null && semester != null) {
                viewModel.createDatesheet(session.sessionId, semester, defaultStart, defaultEnd, defaultBuildingId, instructions)
            }
        },
        openDatesheetId = openDatesheetId,
        onOpenDatesheet = viewModel::openDatesheet,
        detail = detail,
        detailBusy = detailBusy,
        onSetPublished = { published -> editorController?.setPublished(published) },
        onDeleteDatesheet = { editorController?.deleteDatesheet(); viewModel.openDatesheet(null) },
        onSyncMissingSubjects = { editorController?.syncMissingSubjects() },
        onRemovePaper = { slotId -> editorController?.removePaper(slotId) },
        onUpdatePaper = { slot -> editorController?.updatePaper(slot) },
        modifier = modifier,
    )
}

@Composable
private fun collectDatesheetDetail(ec: DatesheetEditorController, departments: List<Department>): DatesheetDetailData? {
    val sheet by ec.sheet.collectAsState()
    val session by ec.session.collectAsState()
    val slots by ec.slots.collectAsState()
    val quality by ec.quality.collectAsState()
    val drift by ec.curriculumDrift.collectAsState()
    val buildings by ec.buildings.collectAsState()
    val rooms by ec.rooms.collectAsState()
    val teachers by ec.teachers.collectAsState()
    val currentSheet = sheet ?: return null
    val department = departments.firstOrNull { it.deptId == session?.deptId }
    return DatesheetDetailData(currentSheet, session, department, slots, quality, drift, buildings, rooms, teachers)
}

@Composable
private fun collectBusy(ec: DatesheetEditorController): Boolean {
    val busy by ec.busy.collectAsState()
    return busy
}
