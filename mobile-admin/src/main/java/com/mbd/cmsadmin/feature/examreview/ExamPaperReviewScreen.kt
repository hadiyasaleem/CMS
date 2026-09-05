package com.mbd.cmsadmin.feature.examreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.ExamPaperReviewController
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.ui.components.ExamPaperReviewWorkspace
import com.mbd.cmscommon.util.FileOpener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExamPaperReviewViewModel @Inject constructor(
    repository: ExamPaperSubmissionRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    val controller = ExamPaperReviewController(
        repo = repository,
        reviewedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )
}

@Composable
fun ExamPaperReviewScreen(viewModel: ExamPaperReviewViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val context = LocalContext.current
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
            controller.downloadAndOpen(submission, context.cacheDir) { file ->
                FileOpener.open(context, file, "application/pdf")
            }
        },
        onMarkReviewed = controller::markReviewed,
        onConsumeNotice = controller::consumeNotice,
        onClearError = controller::clearError,
        onRefresh = controller::refresh,
    )
}
