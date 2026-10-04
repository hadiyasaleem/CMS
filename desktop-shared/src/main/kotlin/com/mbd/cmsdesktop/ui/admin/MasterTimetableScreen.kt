package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.MasterTimetableController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.MasterTimetableWorkspace
import com.mbd.cmsdesktop.platform.rememberDocumentExport

@Composable
fun MasterTimetableScreen(
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    onOpenSession: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(departmentRepository, sessionRepository, timetableRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository) {
        MasterTimetableController(departmentRepository, sessionRepository, timetableRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, scope)
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
    val periodConflicts by controller.periodConflicts.collectAsState()
    val loading by controller.loading.collectAsState()
    val refreshError by controller.refreshError.collectAsState()
    // savePeriod/applyShifts failures land here (ScreenController.launch's own catch), separately
    // from refreshError -- surfaced too, since a rejected edit (e.g. a scheduling conflict) must not
    // fail silently.
    val actionError by controller.error.collectAsState()
    val errorMessage = actionError ?: refreshError
    val errorTitle = if (actionError != null) "Couldn't save this change" else "Couldn't load timetable"
    val actionMessage by controller.actionMessage.collectAsState()

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
        errorMessage = errorMessage,
        errorTitle = errorTitle,
        actionMessage = actionMessage,
        onConsumeActionMessage = controller::consumeActionMessage,
        onSelectSemester = controller::selectSemester,
        onSelectShift = controller::selectShift,
        onSelectDepartment = controller::selectDepartment,
        onSelectProgramType = controller::selectProgramType,
        onClearFilters = controller::clearFilters,
        onRetry = controller::refresh,
        onOpenSession = onOpenSession,
        onSaveShifts = controller::applyShifts,
        onExport = rememberDocumentExport(),
        teachers = teachers,
        buildings = buildings,
        rooms = rooms,
        onLoadSubjects = controller::subjectsFor,
        onSavePeriod = { replaces, days, start, end, subject, teacher, type, room, building, notes, from, to ->
            controller.savePeriod(replaces, days, start, end, subject, teacher, type, room, building, notes, from, to)
        },
    )
}
