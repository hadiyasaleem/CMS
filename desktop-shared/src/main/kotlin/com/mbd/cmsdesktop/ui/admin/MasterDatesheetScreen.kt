package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.MasterDatesheetController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MasterDatesheetWorkspace
import com.mbd.cmsdesktop.platform.rememberDocumentExport

@Composable
fun MasterDatesheetScreen(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    datesheetRepository: DatesheetRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    onOpenSession: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(departmentRepository, sessionRepository, datesheetRepository, teacherRepository, buildingRepository, roomRepository) {
        MasterDatesheetController(departmentRepository, sessionRepository, datesheetRepository, teacherRepository, buildingRepository, roomRepository, scope)
    }
    val teachers by controller.teachers.collectAsState()
    val buildings by controller.buildings.collectAsState()
    val rooms by controller.rooms.collectAsState()
    val departments by controller.departments.collectAsState()
    val availableSemesters by controller.availableSemesters.collectAsState()
    val selectedSemester by controller.selectedSemester.collectAsState()
    val selectedShift by controller.selectedShift.collectAsState()
    val selectedDeptId by controller.selectedDeptId.collectAsState()
    val selectedProgramType by controller.selectedProgramType.collectAsState()
    val grids by controller.filteredGrids.collectAsState()
    val loading by controller.loading.collectAsState()
    val refreshError by controller.refreshError.collectAsState()
    val actionError by controller.error.collectAsState()
    val busy by controller.busy.collectAsState()

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
        onSelectSemester = controller::selectSemester,
        onSelectShift = controller::selectShift,
        onSelectDepartment = controller::selectDepartment,
        onSelectProgramType = controller::selectProgramType,
        onClearFilters = controller::clearFilters,
        onRetry = controller::refresh,
        onOpenSession = onOpenSession,
        buildings = buildings,
        rooms = rooms,
        teachers = teachers,
        busy = busy,
        assignErrorMessage = actionError,
        onAssignPaper = controller::assignPaperToDate,
        onExport = rememberDocumentExport(),
    )
}
