package com.mbd.cmsadmin.feature.students

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentRecordController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentRecordWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudentRecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    curriculumRepository: CurriculumRepository,
    attendanceRepository: SessionAttendanceRepository,
    marksRepository: SessionMarksRepository,
    feeRepository: SessionFeeRepository,
    fineRepository: FineRepository,
) : ViewModel() {
    val controller = StudentRecordController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        rollNumber = checkNotNull(savedStateHandle["roll"]),
        sessionRepository = sessionRepository,
        departmentRepository = departmentRepository,
        curriculumRepository = curriculumRepository,
        attendanceRepository = attendanceRepository,
        marksRepository = marksRepository,
        feeRepository = feeRepository,
        fineRepository = fineRepository,
        scope = viewModelScope,
    )
}

@Composable
fun StudentRecordScreen(
    onBack: () -> Unit,
    onEditProfile: (sessionId: String, rollNumber: String) -> Unit,
    viewModel: StudentRecordViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller
    val record by controller.record.collectAsState()
    val loading by controller.loading.collectAsState()
    val notFound by controller.notFound.collectAsState()
    val error by controller.error.collectAsState()

    StudentRecordWorkspace(
        rollNumber = controller.rollNumber,
        record = record,
        loading = loading,
        notFound = notFound,
        errorMessage = error,
        onBack = onBack,
        onEditProfile = { onEditProfile(controller.sessionId, controller.rollNumber) },
        onRetry = controller::refresh,
        onClearError = controller::clearError,
        onExport = rememberDocumentExport(),
    )
}
