package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.StudentAttendanceSummaryController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.ui.components.StudentAttendanceSummaryWorkspace
import com.mbd.cmsdesktop.platform.DocumentExporter

@Composable
fun StudentAttendanceSummaryScreen(
    sessionId: String,
    courseCode: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    attendanceRepository: SessionAttendanceRepository,
    window: ComposeWindow,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, courseCode, rollNumber) {
        StudentAttendanceSummaryController(sessionId, courseCode, rollNumber, sessionRepository, curriculumRepository, attendanceRepository, scope)
    }
    val student by controller.student.collectAsState()
    val session by controller.session.collectAsState()
    val term by controller.term.collectAsState()
    val summary by controller.summary.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()

    StudentAttendanceSummaryWorkspace(
        courseCode = courseCode,
        rollNumber = rollNumber,
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
                runCatching { DocumentExporter.export(window, doc, format) }
                    .onFailure { controller.reportFailure(it, "Could not export the attendance summary.") }
            }
        },
    )
}
