package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Admin's exam-paper review queue: every submission still awaiting review, across all sessions. */
class ExamPaperReviewController(
    private val repo: ExamPaperSubmissionRepository,
    private val reviewedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _pending = MutableStateFlow<List<ExamPaperSubmission>>(emptyList())
    val pending: StateFlow<List<ExamPaperSubmission>> = _pending.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busySubmissionId = MutableStateFlow<String?>(null)
    val busySubmissionId: StateFlow<String?> = _busySubmissionId.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launch {
        _loading.value = true
        try {
            _pending.value = repo.getPendingReview()
        } finally {
            _loading.value = false
        }
    }

    fun markReviewed(submission: ExamPaperSubmission, notes: String?) = launch {
        try {
            _busySubmissionId.value = submission.submissionId
            require(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }
            repo.markReviewed(submission.submissionId, reviewedBy, notes)
            _pending.value = _pending.value.filterNot { it.submissionId == submission.submissionId }
            _notice.value = "\"${submission.fileName}\" marked as reviewed."
        } finally {
            _busySubmissionId.value = null
        }
    }

    fun downloadAndOpen(submission: ExamPaperSubmission, targetDir: File, opener: (File) -> Unit) = launch {
        val file = repo.downloadTo(submission, targetDir)
        opener(file)
    }

    fun consumeNotice() {
        _notice.value = null
    }
}
