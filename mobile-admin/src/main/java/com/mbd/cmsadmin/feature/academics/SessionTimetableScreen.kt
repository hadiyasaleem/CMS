package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SessionTimetableController
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.SessionTimetableWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SessionTimetableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    timetableRepository: SessionTimetableRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
) : ViewModel() {
    private val controller = SessionTimetableController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        timetableRepository = timetableRepository,
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        teacherRepository = teacherRepository,
        buildingRepository = buildingRepository,
        roomRepository = roomRepository,
        scope = viewModelScope,
    )

    val session = controller.session
    val periods = controller.periods
    val subjects = controller.subjects
    val teachers = controller.teachers
    val buildings = controller.buildings
    val rooms = controller.rooms
    val currentSemesterTerm = controller.currentSemesterTerm
    val error = controller.error

    fun savePeriod(
        day: DayOfWeek,
        start: String,
        end: String,
        subject: SemesterSubject?,
        teacher: Teacher?,
        type: PeriodType,
        room: String?,
        building: String?,
        notes: String?,
        effectiveFrom: LocalDate?,
        effectiveTo: LocalDate?,
        replaces: SessionPeriod?,
    ) = controller.savePeriod(day, start, end, subject, teacher, type, room, building, notes, effectiveFrom, effectiveTo, replaces)

    fun removePeriod(period: SessionPeriod) = controller.removePeriod(period)
    fun clearError() = controller.clearError()
}

@Composable
fun SessionTimetableScreen(viewModel: SessionTimetableViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val buildings by viewModel.buildings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val currentSemesterTerm by viewModel.currentSemesterTerm.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    SessionTimetableWorkspace(
        session = session,
        periods = periods,
        subjects = subjects,
        teachers = teachers,
        buildings = buildings,
        rooms = rooms,
        currentSemesterTerm = currentSemesterTerm,
        errorMessage = errorMessage,
        onSavePeriod = viewModel::savePeriod,
        onRemovePeriod = viewModel::removePeriod,
        onClearError = viewModel::clearError,
    )
}
