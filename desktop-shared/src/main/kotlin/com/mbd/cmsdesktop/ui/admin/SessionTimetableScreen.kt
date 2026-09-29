package com.mbd.cmsdesktop.ui.admin

import com.mbd.cmsdesktop.platform.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SessionTimetableController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SessionTimetableWorkspace

@Composable
fun SessionTimetableScreen(
    sessionId: String,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    timetableRepository: SessionTimetableRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    departmentRepository: DepartmentRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, timetableRepository, sessionRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, departmentRepository) {
        SessionTimetableController(sessionId, timetableRepository, sessionRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, scope, departmentRepository = departmentRepository)
    }
    val session by controller.session.collectAsState()
    val periods by controller.periods.collectAsState()
    val subjects by controller.subjects.collectAsState()
    val teachers by controller.teachers.collectAsState()
    val buildings by controller.buildings.collectAsState()
    val rooms by controller.rooms.collectAsState()
    val currentSemesterTerm by controller.currentSemesterTerm.collectAsState()
    val errorMessage by controller.error.collectAsState()
    val shift by controller.shift.collectAsState()
    val shifts by controller.shifts.collectAsState()
    val allSessions by controller.allSessions.collectAsState()
    val allPeriods by controller.allPeriods.collectAsState()

    SessionTimetableWorkspace(

        onExport = rememberDocumentExport(),
        session = session,
        periods = periods,
        subjects = subjects,
        teachers = teachers,
        buildings = buildings,
        rooms = rooms,
        currentSemesterTerm = currentSemesterTerm,
        errorMessage = errorMessage,
        onSavePeriod = controller::savePeriod,
        onRemovePeriod = controller::removePeriod,
        onClearError = controller::clearError,
        shift = shift,
        shifts = shifts,
        onSelectShift = controller::selectShift,
        allSessions = allSessions,
        allPeriods = allPeriods,
        onSetLink = controller::setPeriodLink,
        onMergeExisting = controller::mergeExistingPeriod,
    )
}
