package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SessionDetailController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import com.mbd.cmscommon.ui.components.SessionOperationsWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    timetableRepository: SessionTimetableRepository,
    feeRepository: SessionFeeRepository,
    teacherRepository: TeacherRepository,
) : ViewModel() {
    private val controller = SessionDetailController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        timetableRepository = timetableRepository,
        feeRepository = feeRepository,
        scope = viewModelScope,
    )

    val sessionId = controller.sessionId
    val session = controller.session
    val students = controller.students
    val subjectCounts = controller.subjectCounts
    val periods = controller.periods
    val fee = controller.fee
    val feeLoading = controller.feeLoading
    val error = controller.error
    val teachers = teacherRepository.observeActiveTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setSemester(semester: Int) = controller.setSemester(semester)
    fun updateDetails(programName: String?, inchargeEmail: String?, maxStudents: Int) =
        controller.updateDetails(programName, inchargeEmail, maxStudents)
    fun deleteSession(onDone: () -> Unit) = controller.deleteSession(onDone)
    fun clearError() = controller.clearError()
}

@Composable
fun SessionDetailScreen(
    onOpenStudents: (String) -> Unit,
    onOpenTimetable: (String) -> Unit,
    onOpenSemester: (String, Int) -> Unit,
    onOpenFees: (String) -> Unit,
    onDeleted: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsState()
    val students by viewModel.students.collectAsState()
    val subjectCounts by viewModel.subjectCounts.collectAsState()
    val periods by viewModel.periods.collectAsState()
    val fee by viewModel.fee.collectAsState()
    val feeLoading by viewModel.feeLoading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val teachers by viewModel.teachers.collectAsState()

    SessionOperationsWorkspace(
        session = session,
        students = students,
        subjectCounts = subjectCounts,
        periods = periods,
        fee = fee,
        feeLoading = feeLoading,
        errorMessage = errorMessage,
        teachers = teachers,
        onSetSemester = viewModel::setSemester,
        onUpdateDetails = viewModel::updateDetails,
        onOpenStudents = { onOpenStudents(viewModel.sessionId) },
        onOpenTimetable = { onOpenTimetable(viewModel.sessionId) },
        onOpenSemester = { onOpenSemester(viewModel.sessionId, it) },
        onOpenFees = { onOpenFees(viewModel.sessionId) },
        onDeleteSession = { viewModel.deleteSession(onDeleted) },
        onClearError = viewModel::clearError,
    )
}
