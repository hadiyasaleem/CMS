package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.MasterGrid
import com.mbd.cmscommon.controller.MasterTimetableController
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MasterTimetableWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MasterTimetableViewModel @Inject constructor(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
) : ViewModel() {
    private val controller = MasterTimetableController(
        departmentRepository,
        sessionRepository,
        timetableRepository,
        curriculumRepository,
        teacherRepository,
        buildingRepository,
        roomRepository,
        viewModelScope,
    )

    val departments = controller.departments
    val availableSemesters = controller.availableSemesters
    val selectedSemester = controller.selectedSemester
    val selectedShift = controller.selectedShift
    val selectedDeptId = controller.selectedDeptId
    val selectedProgramType = controller.selectedProgramType
    val allSessions = controller.sessions
    val grids = controller.filteredGrids
    val periodConflicts = controller.periodConflicts
    val loading = controller.loading
    val refreshError = controller.refreshError
    // savePeriod/applyShifts failures land here (ScreenController.launch's own catch), separately
    // from refreshError -- surfaced too, since a rejected edit (e.g. a scheduling conflict) must not
    // fail silently.
    val actionError = controller.error
    val actionMessage = controller.actionMessage
    val teachers = controller.teachers
    val buildings = controller.buildings
    val rooms = controller.rooms

    fun selectSemester(semester: Int?) = controller.selectSemester(semester)
    fun selectShift(shift: Session?) = controller.selectShift(shift)
    fun selectDepartment(deptId: String?) = controller.selectDepartment(deptId)
    fun selectProgramType(programType: ProgramType?) = controller.selectProgramType(programType)
    fun clearFilters() = controller.clearFilters()
    fun refresh() = controller.refresh()
    fun applyShifts(grid: MasterGrid, shifts: Map<Pair<String, String>, Pair<String, String>>) = controller.applyShifts(grid, shifts)
    suspend fun subjectsFor(sessionId: String, semester: Int): List<SemesterSubject> = controller.subjectsFor(sessionId, semester)
    fun savePeriod(
        replaces: SessionPeriod,
        days: Set<DayOfWeek>,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teachers: List<Teacher>,
        periodType: PeriodType,
        roomNo: String,
        building: String,
        notes: String,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
    ) = controller.savePeriod(replaces, days, start, end, subject, teachers, periodType, roomNo, building, notes, effectiveFrom, effectiveTo)
    fun consumeActionMessage() = controller.consumeActionMessage()
    fun setPeriodLink(period: SessionPeriod, targetSessionId: String, link: Boolean) = controller.setPeriodLink(period, targetSessionId, link)
    fun unmergeSession(
        period: SessionPeriod,
        unlinkSessionId: String,
        days: Set<DayOfWeek>,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teachers: List<Teacher>,
        periodType: PeriodType,
        roomNo: String,
        building: String,
        notes: String,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
    ) = controller.unmergeSession(period, unlinkSessionId, days, start, end, subject, teachers, periodType, roomNo, building, notes, effectiveFrom, effectiveTo)
}

@Composable
fun MasterTimetableScreen(
    onOpenSession: (String) -> Unit,
    viewModel: MasterTimetableViewModel = hiltViewModel(),
) {
    // Room's cached grids render instantly if present; this kicks off the network sync behind them
    // so a genuinely first-ever open (nothing cached yet) shows the loading skeleton instead of the
    // empty-state card.
    LaunchedEffect(Unit) { viewModel.refresh() }

    val departments by viewModel.departments.collectAsState()
    val availableSemesters by viewModel.availableSemesters.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()
    val selectedDeptId by viewModel.selectedDeptId.collectAsState()
    val selectedProgramType by viewModel.selectedProgramType.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val grids by viewModel.grids.collectAsState()
    val periodConflicts by viewModel.periodConflicts.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val refreshError by viewModel.refreshError.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val error = actionError ?: refreshError
    val errorTitle = if (actionError != null) "Couldn't save this change" else "Couldn't load timetable"
    val actionMessage by viewModel.actionMessage.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    MasterTimetableWorkspace(
        departments = departments,
        availableSemesters = availableSemesters,
        selectedSemester = selectedSemester,
        selectedShift = selectedShift,
        selectedDeptId = selectedDeptId,
        selectedProgramType = selectedProgramType,
        grids = grids,
        periodConflicts = periodConflicts,
        loading = loading,
        errorMessage = error,
        errorTitle = errorTitle,
        actionMessage = actionMessage,
        onConsumeActionMessage = viewModel::consumeActionMessage,
        onSelectSemester = viewModel::selectSemester,
        onSelectShift = viewModel::selectShift,
        onSelectDepartment = viewModel::selectDepartment,
        onSelectProgramType = viewModel::selectProgramType,
        onClearFilters = viewModel::clearFilters,
        onRetry = viewModel::refresh,
        onOpenSession = onOpenSession,
        onSaveShifts = viewModel::applyShifts,
        onExport = rememberDocumentExport(),
        teachers = teachers,
        buildings = buildings,
        rooms = rooms,
        onLoadSubjects = viewModel::subjectsFor,
        onSavePeriod = { replaces, days, start, end, subject, teacher, type, room, building, notes, from, to ->
            viewModel.savePeriod(replaces, days, start, end, subject, teacher, type, room, building, notes, from, to)
        },
        allSessions = allSessions,
        onSetLink = viewModel::setPeriodLink,
        onUnmergeSession = { period, sid, days, start, end, subject, teacher, type, room, building, notes, from, to ->
            viewModel.unmergeSession(period, sid, days, start, end, subject, teacher, type, room, building, notes, from, to)
        },
    )
}
