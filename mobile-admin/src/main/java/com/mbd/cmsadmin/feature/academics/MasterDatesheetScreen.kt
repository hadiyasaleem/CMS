package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.MasterDatesheetController
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MasterDatesheetWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MasterDatesheetViewModel @Inject constructor(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    datesheetRepository: DatesheetRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
) : ViewModel() {
    private val controller = MasterDatesheetController(
        departmentRepository,
        sessionRepository,
        datesheetRepository,
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
    val grids = controller.filteredGrids
    val loading = controller.loading
    val refreshError = controller.refreshError
    // assignPaperToDate failures land here (ScreenController.launch's own catch), separate from
    // refreshError -- surfaced too, since a rejected scheduling attempt must not fail silently.
    val actionError = controller.error
    val busy = controller.busy
    val teachers = controller.teachers
    val buildings = controller.buildings
    val rooms = controller.rooms

    fun selectSemester(semester: Int?) = controller.selectSemester(semester)
    fun selectShift(shift: Session?) = controller.selectShift(shift)
    fun selectDepartment(deptId: String?) = controller.selectDepartment(deptId)
    fun selectProgramType(programType: ProgramType?) = controller.selectProgramType(programType)
    fun clearFilters() = controller.clearFilters()
    fun refresh() = controller.refresh()
    fun assignPaperToDate(sheet: Datesheet, slot: DatesheetSlot) = controller.assignPaperToDate(sheet, slot)
}

@Composable
fun MasterDatesheetScreen(
    onOpenSession: (String) -> Unit,
    viewModel: MasterDatesheetViewModel = hiltViewModel(),
) {
    val departments by viewModel.departments.collectAsState()
    val availableSemesters by viewModel.availableSemesters.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()
    val selectedDeptId by viewModel.selectedDeptId.collectAsState()
    val selectedProgramType by viewModel.selectedProgramType.collectAsState()
    val grids by viewModel.grids.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val refreshError by viewModel.refreshError.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()

    MasterDatesheetWorkspace(
        departments = departments,
        availableSemesters = availableSemesters,
        selectedSemester = selectedSemester,
        selectedShift = selectedShift,
        selectedDeptId = selectedDeptId,
        selectedProgramType = selectedProgramType,
        grids = grids,
        loading = loading,
        errorMessage = refreshError,
        onSelectSemester = viewModel::selectSemester,
        onSelectShift = viewModel::selectShift,
        onSelectDepartment = viewModel::selectDepartment,
        onSelectProgramType = viewModel::selectProgramType,
        onClearFilters = viewModel::clearFilters,
        onRetry = viewModel::refresh,
        onOpenSession = onOpenSession,
        buildings = buildings,
        rooms = rooms,
        teachers = teachers,
        busy = busy,
        assignErrorMessage = actionError,
        onAssignPaper = viewModel::assignPaperToDate,
        onExport = rememberDocumentExport(),
    )
}
