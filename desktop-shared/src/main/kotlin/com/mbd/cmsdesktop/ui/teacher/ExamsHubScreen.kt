package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.ExamsHubController
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.ExamsDestination
import com.mbd.cmscommon.ui.components.ExamsHubWorkspace

/** Exams tab: dashboard for Marks / Exam paper / Results / Datesheets. */
@Composable
fun ExamsHubScreen(
    teacherId: String,
    assignmentsProvider: TeacherAssignmentsProvider,
    examPaperRepository: ExamPaperSubmissionRepository,
    datesheetRepository: DatesheetRepository,
    onOpen: (ExamsDestination) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(teacherId, assignmentsProvider, examPaperRepository, datesheetRepository) {
        ExamsHubController(teacherId, assignmentsProvider, examPaperRepository, datesheetRepository, scope)
    }
    val snapshot by controller.snapshot.collectAsState()
    val loading by controller.loading.collectAsState()
    val loadError by controller.loadError.collectAsState()

    ExamsHubWorkspace(
        heroPainter = painterResource("teacher-exams-hero.jpg"),
        snapshot = snapshot,
        loading = loading,
        errorMessage = loadError,
        onRetry = controller::refresh,
        onOpen = onOpen,
    )
}
