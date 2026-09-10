package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.ExamPaperSubmissionController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.ExamPaperSubmissionWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File

/** Exam-paper upload leaf: file picking / opening goes through [AwtDesktopPlatformServices]. */
@Composable
fun ExamPaperSubmissionScreen(
    teacherId: String,
    examPaperRepository: ExamPaperSubmissionRepository,
    datesheetRepository: DatesheetRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    academicSessionRepository: AcademicSessionRepository,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(examPaperRepository, datesheetRepository, teacherId) {
        ExamPaperSubmissionController(examPaperRepository, datesheetRepository, assignmentsProvider, academicSessionRepository, teacherId, scope)
    }
    val slots by controller.slots.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val selected by controller.selected.collectAsState()
    val stagedFile by controller.stagedFile.collectAsState()
    val uploadState by controller.uploadState.collectAsState()

    ExamPaperSubmissionWorkspace(
        slots = slots,
        sessions = sessions,
        selected = selected,
        stagedFile = stagedFile,
        uploadState = uploadState,
        onSelectSlot = controller::selectSlot,
        onChooseFile = {
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose the exam paper (PDF)")
            if (file != null) {
                try {
                    controller.stageFile(file.readBytes(), file.name)
                } catch (t: Throwable) {
                    controller.reportPickFailure(t)
                }
            }
        },
        onClearStagedFile = controller::clearStagedFile,
        onConfirmUpload = controller::confirmUpload,
        onOpen = { submission ->
            controller.downloadAndOpen(submission, File(System.getProperty("java.io.tmpdir"))) { downloaded ->
                AwtDesktopPlatformServices.open(downloaded)
                Unit
            }
        },
        onDelete = controller::deleteSubmission,
    )
}
