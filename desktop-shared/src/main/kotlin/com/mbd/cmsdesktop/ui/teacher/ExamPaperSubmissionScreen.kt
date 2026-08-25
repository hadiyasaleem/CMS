package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.ExamPaperSubmissionController
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.ExamPaperSubmissionWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File

/** Exam-paper upload leaf: file picking / opening goes through [AwtDesktopPlatformServices]. */
@Composable
fun ExamPaperSubmissionScreen(
    teacherId: String,
    examPaperRepository: ExamPaperSubmissionRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(examPaperRepository, teacherId) {
        ExamPaperSubmissionController(examPaperRepository, teacherId, scope)
    }
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
    val selected by controller.selected.collectAsState()
    val examType by controller.examType.collectAsState()
    val submissions by controller.submissions.collectAsState()
    val uploadState by controller.uploadState.collectAsState()

    ExamPaperSubmissionWorkspace(
        assignments = assignments,
        selected = selected,
        examType = examType,
        submissions = submissions,
        outcome = uploadState ?: Outcome.Success(Unit),
        onSelect = controller::select,
        onExamType = controller::selectExamType,
        onChooseFile = {
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose the exam paper (PDF/DOCX)")
            if (file != null) {
                controller.upload(file.readBytes(), file.name)
            }
        },
        onOpen = { submission ->
            controller.downloadAndOpen(submission, File(System.getProperty("java.io.tmpdir"))) { downloaded ->
                AwtDesktopPlatformServices.open(downloaded)
                Unit
            }
        },
        onDelete = controller::deleteSubmission,
    )
}
