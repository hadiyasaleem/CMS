package com.mbd.cmsteacher.feature.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentAttendanceSummaryController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.ui.components.StudentAttendanceSummaryWorkspace
import com.mbd.cmscommon.util.DocumentExporter
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudentAttendanceSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    attendanceRepository: SessionAttendanceRepository,
) : ViewModel() {
    val controller = StudentAttendanceSummaryController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        courseCode = checkNotNull(savedStateHandle["courseCode"]),
        rollNumber = checkNotNull(savedStateHandle["rollNumber"]),
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        attendanceRepository = attendanceRepository,
        scope = viewModelScope,
    )
}

@Composable
fun StudentAttendanceSummaryScreen(
    onBack: () -> Unit,
    viewModel: StudentAttendanceSummaryViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val student by controller.student.collectAsState()
    val session by controller.session.collectAsState()
    val term by controller.term.collectAsState()
    val summary by controller.summary.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()

    StudentAttendanceSummaryWorkspace(
        courseCode = controller.courseCode,
        rollNumber = controller.rollNumber,
        student = student,
        session = session,
        term = term,
        summary = summary,
        loading = loading,
        errorMessage = error,
        onBack = onBack,
        onRetry = controller::refresh,
        onClearError = controller::clearError,
        onExport = { format ->
            controller.exportDocument()?.let { doc ->
                scope.launch {
                    runCatching { DocumentExporter.export(context, doc, format) }
                        .onFailure { controller.reportFailure(it, "Could not export the attendance summary.") }
                }
            }
        },
    )
}
