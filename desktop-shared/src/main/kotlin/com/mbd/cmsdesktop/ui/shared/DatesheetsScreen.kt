package com.mbd.cmsdesktop.ui.shared

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
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
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
import com.mbd.cmscommon.ui.components.StudentDatesheetWorkspace
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Admin/teacher Datesheets screen: builds its own [DatesheetBrowseController] for the
 * department/session/semester filters and grid views, and a [DatesheetEditorController] scoped to
 * whichever datesheet is currently open.
 */
@Composable
fun DatesheetsScreen(
    datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    viewer: DatesheetViewerContext,
    createdBy: String,
) {
    val scope = rememberCoroutineScope()
    val browseController = remember(datesheetRepository) {
        DatesheetBrowseController(datesheetRepository, sessionRepository, departmentRepository, scope)
    }
    LaunchedEffect(browseController) { browseController.refresh() }

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
    val selectedSemester by browseController.selectedSemester.collectAsState()
    val sessionsInDepartment by browseController.sessionsInDepartment.collectAsState()
    val shiftsForSelection by browseController.shiftsForSelection.collectAsState()
    val resolvedSession by browseController.resolvedSession.collectAsState()
    val browseError by browseController.error.collectAsState()
    val allSlots by datesheetRepository.observeAllSlots().collectAsState(initial = emptyList())
    val buildings by buildingRepository.observeActiveBuildings().collectAsState(initial = emptyList())

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
        onSelectDepartment = browseController::selectDepartment,
        onSelectStartYear = browseController::selectStartYear,
        onSelectShift = browseController::selectShift,
        onSelectSemester = browseController::selectSemester,
        buildings = buildings,
        loading = false,
        errorMessage = browseError ?: detailError,
        onRetry = { browseController.refresh() },
        onCreateDatesheet = { defaultStart, defaultEnd, defaultBuildingId, instructions ->
            val session = resolvedSession
            val semester = selectedSemester
            if (session != null && semester != null) {
                scope.launch {
                    runCatching {
                        val id = browseController.createDatesheet(session.sessionId, semester, defaultStart, defaultEnd, defaultBuildingId, instructions, createdBy)
                        openDatesheetId = id
                    }
                }
            }
        },
        openDatesheetId = openDatesheetId,
        onOpenDatesheet = { openDatesheetId = it },
        detail = detail,
        detailBusy = detailBusy,
        onSetPublished = { published -> editorController?.setPublished(published) },
        onDeleteDatesheet = { editorController?.deleteDatesheet(); openDatesheetId = null },
        onSyncMissingSubjects = { editorController?.syncMissingSubjects() },
        onRemovePaper = { slotId -> editorController?.removePaper(slotId) },
        onUpdatePaper = { slot -> editorController?.updatePaper(slot) },
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

/** Read-only single-datesheet view for the student role: their own session's current semester's papers. */
@Composable
fun StudentDatesheetsScreen(
    sessionId: String,
    datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
) {
    val session by sessionRepository.observeSession(sessionId).collectAsState(initial = null)
    val semester = session?.currentSemester
    val sheet by datesheetRepository.observeDatesheets()
        .map { sheets -> sheets.firstOrNull { it.sessionId == sessionId && it.semester == semester && it.published } }
        .collectAsState(initial = null as Datesheet?)
    val allSlots by datesheetRepository.observeAllSlots().collectAsState(initial = emptyList())
    val slots = sheet?.let { s -> allSlots.filter { it.datesheetId == s.id } }.orEmpty()

    LaunchedEffect(datesheetRepository) {
        runCatching { datesheetRepository.sync(); datesheetRepository.syncAllSlots() }
    }

    StudentDatesheetWorkspace(
        sheet = sheet,
        session = session,
        slots = slots,
        loading = false,
        errorMessage = null,
        onRetry = {},
    )
}
