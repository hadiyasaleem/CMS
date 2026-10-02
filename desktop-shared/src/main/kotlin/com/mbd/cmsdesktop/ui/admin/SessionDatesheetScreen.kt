package com.mbd.cmsdesktop.ui.admin

import com.mbd.cmsdesktop.platform.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.controller.DatesheetBrowseController
import com.mbd.cmscommon.controller.DatesheetEditorController
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.DatesheetDetailData
import com.mbd.cmscommon.ui.components.DatesheetWorkspace
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** One session's own Morning/Evening datesheet(s) -- reached from Session Detail -> Datesheet,
 * the same relationship Session Detail -> Timetable has to Records -> Master Timetable. */
@Composable
fun SessionDatesheetScreen(
    sessionId: String,
    datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    createdBy: String,
) {
    val scope = rememberCoroutineScope()
    val browseController = remember(datesheetRepository) {
        DatesheetBrowseController(datesheetRepository, sessionRepository, departmentRepository, scope)
    }
    LaunchedEffect(browseController) {
        browseController.refresh()
        val session = browseController.sessions
            .map { list -> list.firstOrNull { it.sessionId == sessionId } }
            .filterNotNull()
            .first()
        browseController.selectDepartment(session.deptId)
        browseController.selectStartYear(session.startYear)
    }

    var openDatesheetId by remember { mutableStateOf<String?>(null) }
    val editorController = openDatesheetId?.let { id ->
        remember(id) {
            DatesheetEditorController(id, datesheetRepository, sessionRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, scope)
        }
    }

    val departments by browseController.departments.collectAsState()
    val sessions by browseController.sessions.collectAsState()
    val datesheets by browseController.datesheets.collectAsState()
    val selectedDeptId by browseController.selectedDeptId.collectAsState()
    val selectedStartYear by browseController.selectedStartYear.collectAsState()
    val selectedShift by browseController.selectedShift.collectAsState()
    val sessionsInDepartment by browseController.sessionsInDepartment.collectAsState()
    val shiftsForSelection by browseController.shiftsForSelection.collectAsState()
    val resolvedSession by browseController.resolvedSession.collectAsState()
    val browseError by browseController.error.collectAsState()
    val allSlots by datesheetRepository.observeAllSlots().collectAsState(initial = emptyList())
    val buildings by buildingRepository.observeActiveBuildings().collectAsState(initial = emptyList())
    val rooms by roomRepository.observeActiveRooms().collectAsState(initial = emptyList())

    val detail: DatesheetDetailData? = editorController?.let { collectSessionDatesheetDetail(it, departments) }
    val detailBusy = editorController?.let { collectSessionDatesheetBusy(it) } ?: false
    val detailError = editorController?.error?.collectAsState()?.value

    DatesheetWorkspace(

        onExport = rememberDocumentExport(),
        viewer = DatesheetViewerContext(DatesheetViewerRole.ADMIN, canManage = true),
        departments = departments,
        sessions = sessions,
        datesheets = datesheets,
        allSlots = allSlots,
        selectedDeptId = selectedDeptId,
        selectedStartYear = selectedStartYear,
        selectedShift = selectedShift,
        sessionsInDepartment = sessionsInDepartment,
        shiftsForSelection = shiftsForSelection,
        resolvedSession = resolvedSession,
        onSelectDepartment = browseController::selectDepartment,
        onSelectStartYear = browseController::selectStartYear,
        onSelectShift = browseController::selectShift,
        buildings = buildings,
        rooms = rooms,
        loading = false,
        errorMessage = browseError ?: detailError,
        onRetry = { browseController.refresh() },
        onCreateDatesheet = { defaultStart, defaultEnd, defaultBuildingId, defaultRoomId, instructions ->
            val session = resolvedSession
            if (session != null) {
                scope.launch {
                    runCatching {
                        val id = browseController.createDatesheet(session.sessionId, session.currentSemester, defaultStart, defaultEnd, defaultBuildingId, defaultRoomId, instructions, createdBy)
                        openDatesheetId = id
                    }
                }
            }
        },
        openDatesheetId = openDatesheetId,
        onOpenDatesheet = { openDatesheetId = it },
        detail = detail,
        detailBusy = detailBusy,
        detailErrorMessage = detailError,
        onSetPublished = { published -> editorController?.setPublished(published) },
        onDeleteDatesheet = { editorController?.deleteDatesheet(); openDatesheetId = null },
        onSyncMissingSubjects = { editorController?.syncMissingSubjects() },
        onRemovePaper = { slotId -> editorController?.removePaper(slotId) },
        onUpdatePaper = { slot -> editorController?.updatePaper(slot) },
        lockedSessionId = sessionId,
    )
}

@Composable
private fun collectSessionDatesheetDetail(ec: DatesheetEditorController, departments: List<Department>): DatesheetDetailData? {
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
private fun collectSessionDatesheetBusy(ec: DatesheetEditorController): Boolean {
    val busy by ec.busy.collectAsState()
    return busy
}
