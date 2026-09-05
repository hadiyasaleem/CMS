package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.ExamPaperReviewController
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.ui.components.ExamPaperReviewWorkspace
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File

@Composable
fun ExamPaperReviewScreen(
    examPaperRepository: ExamPaperSubmissionRepository,
    reviewedBy: String,
    window: ComposeWindow,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(examPaperRepository, reviewedBy) {
        ExamPaperReviewController(examPaperRepository, reviewedBy, scope)
    }
    val pending by controller.pending.collectAsState()
    val loading by controller.loading.collectAsState()
    val busySubmissionId by controller.busySubmissionId.collectAsState()
    val notice by controller.notice.collectAsState()
    val error by controller.error.collectAsState()

    ExamPaperReviewWorkspace(
        pending = pending,
        loading = loading,
        busySubmissionId = busySubmissionId,
        notice = notice,
        errorMessage = error,
        onOpen = { submission ->
            controller.downloadAndOpen(submission, File(System.getProperty("java.io.tmpdir"))) { downloaded ->
                AwtDesktopPlatformServices.open(downloaded)
                Unit
            }
        },
        onMarkReviewed = controller::markReviewed,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
        onRefresh = controller::refresh,
    )
}
