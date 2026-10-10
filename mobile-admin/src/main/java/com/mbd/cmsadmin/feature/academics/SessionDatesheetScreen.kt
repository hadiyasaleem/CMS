package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DatesheetBrowseController
import com.mbd.cmscommon.controller.DatesheetEditorController
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.DatesheetDetailData
import com.mbd.cmscommon.ui.components.DatesheetWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One session's own Morning/Evening datesheet(s) -- reached from Session Detail -> Datesheet,
 * the same relationship Session Detail -> Timetable has to Records -> Master Timetable. */
@HiltViewModel
class SessionDatesheetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val datesheetRepository: DatesheetRepository,
    private val sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    private val curriculumRepository: CurriculumRepository,
    private val teacherRepository: TeacherRepository,
    private val buildingRepository: BuildingRepository,
    private val roomRepository: RoomRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    val browseController = DatesheetBrowseController(datesheetRepository, sessionRepository, departmentRepository, viewModelScope)

    val viewer = DatesheetViewerContext(DatesheetViewerRole.ADMIN, canManage = true)

    val allSlots: StateFlow<List<DatesheetSlot>> =
        datesheetRepository.observeAllSlots().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val buildings: StateFlow<List<Building>> =
        buildingRepository.observeActiveBuildings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val rooms: StateFlow<List<Room>> =
        roomRepository.observeActiveRooms().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _openDatesheetId = MutableStateFlow<String?>(null)
    val openDatesheetId: StateFlow<String?> = _openDatesheetId.asStateFlow()

    private val _editorController = MutableStateFlow<DatesheetEditorController?>(null)
    val editorController: StateFlow<DatesheetEditorController?> = _editorController.asStateFlow()

    init {
        // Pre-select this screen's own session once it shows up in the browse cascade, so the
        // Filtered view opens straight onto it with no department/session picker shown.
        viewModelScope.launch {
            val session = browseController.sessions
                .map { list -> list.firstOrNull { it.sessionId == sessionId } }
                .filterNotNull()
                .first()
            // A single-shift session has only one tab, so pick it; a two-shift session opens on Morning.
            browseController.setFilterScope(
                DeptSemesterScope(session.deptId, session.currentSemester, session.shifts.minOrNull(), session.programType),
            )
        }
    }

    fun openDatesheet(id: String?) {
        _openDatesheetId.value = id
        _editorController.value = id?.let {
            DatesheetEditorController(it, datesheetRepository, sessionRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, viewModelScope)
        }
    }

    fun createDatesheet(semester: Int, defaultStart: String?, defaultEnd: String?, defaultBuildingId: String?, defaultRoomId: String?, instructions: String?) {
        viewModelScope.launch {
            runCatching {
                browseController.createDatesheet(sessionId, semester, defaultStart, defaultEnd, defaultBuildingId, defaultRoomId, instructions, sessionManager.accountKey.orEmpty())
            }.onSuccess { id -> openDatesheet(id) }
        }
    }
}

@Composable
fun SessionDatesheetScreen(viewModel: SessionDatesheetViewModel = hiltViewModel()) {
    val controller = viewModel.browseController
    val departments by controller.departments.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val datesheets by controller.datesheets.collectAsState()
    val filterScope by controller.filterScope.collectAsState()
    val resolvedSession by controller.resolvedSession.collectAsState()
    val browseError by controller.error.collectAsState()
    val allSlots by viewModel.allSlots.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val openDatesheetId by viewModel.openDatesheetId.collectAsState()
    val editorController by viewModel.editorController.collectAsState()

    val detail: DatesheetDetailData? = editorController?.let { collectSessionDatesheetDetail(it, departments) }
    val detailBusy = editorController?.let { collectSessionDatesheetBusy(it) } ?: false
    val detailError = editorController?.error?.collectAsState()?.value

    DatesheetWorkspace(

        onExport = rememberDocumentExport(),
        viewer = viewModel.viewer,
        departments = departments,
        sessions = sessions,
        datesheets = datesheets,
        allSlots = allSlots,
        filterScope = filterScope,
        resolvedSession = resolvedSession,
        onFilterScope = controller::setFilterScope,
        buildings = buildings,
        rooms = rooms,
        loading = false,
        errorMessage = browseError ?: detailError,
        onRetry = { controller.refresh() },
        onCreateDatesheet = { defaultStart, defaultEnd, defaultBuildingId, defaultRoomId, instructions ->
            val session = resolvedSession
            if (session != null) {
                viewModel.createDatesheet(session.currentSemester, defaultStart, defaultEnd, defaultBuildingId, defaultRoomId, instructions)
            }
        },
        openDatesheetId = openDatesheetId,
        onOpenDatesheet = viewModel::openDatesheet,
        detail = detail,
        detailBusy = detailBusy,
        detailErrorMessage = detailError,
        onSetPublished = { published -> editorController?.setPublished(published) },
        onDeleteDatesheet = { editorController?.deleteDatesheet(); viewModel.openDatesheet(null) },
        onSyncMissingSubjects = { editorController?.syncMissingSubjects() },
        onRemovePaper = { slotId -> editorController?.removePaper(slotId) },
        onUpdatePaper = { slot -> editorController?.updatePaper(slot) },
        lockedSessionId = viewModel.sessionId,
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
