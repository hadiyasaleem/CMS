package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ExamPaperSubmissionController(
    private val repo: ExamPaperSubmissionRepository,
    private val teacherId: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _selected = MutableStateFlow<ResolvedAssignment?>(null)
    val selected: StateFlow<ResolvedAssignment?> = _selected.asStateFlow()

    private val _examType = MutableStateFlow(ExamType.MIDTERM)
    val examType: StateFlow<ExamType> = _examType.asStateFlow()

    val submissions: StateFlow<List<ExamPaperSubmission>> = _selected
        .flatMapLatest { assignment ->
            if (assignment == null) flowOf(emptyList()) else repo.observeSubmissionsForOffering(assignment.sessionId, assignment.courseCode)
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uploadState = MutableStateFlow<Outcome<Unit>?>(null)
    val uploadState: StateFlow<Outcome<Unit>?> = _uploadState.asStateFlow()

    fun select(assignment: ResolvedAssignment) {
        _selected.value = assignment
        _uploadState.value = null

    }

    fun selectExamType(type: ExamType) {
        _examType.value = type
    }

    fun upload(fileBytes: ByteArray, fileName: String) {
        val assignment = _selected.value ?: return
        if (_uploadState.value is Outcome.Loading) return // single-flight: block a double-tap duplicate upload
        val message = examPaperUploadError(fileName, fileBytes)
        if (message != null) {
            _uploadState.value = Outcome.Error(message)
            return
        }
        _uploadState.value = Outcome.Loading // set before launch so the guard above sees it synchronously
        launch {
            _uploadState.value = runCatching {
                repo.uploadSubmission(assignment.sessionId, assignment.courseCode, _examType.value, teacherId, fileBytes, fileName)
            }.fold(
                onSuccess = { Outcome.Success(Unit) },
                onFailure = { Outcome.Error(it.userMessage("Upload failed."), it) },
            )
        }
    }

    fun deleteSubmission(submissionId: String) = launch {
        runCatching { repo.deleteSubmission(submissionId) }
            .onFailure { _uploadState.value = Outcome.Error(it.userMessage("Could not delete the submission."), it) }
    }

    fun downloadAndOpen(submission: ExamPaperSubmission, targetDir: File, opener: (File) -> Unit) = launch {
        runCatching {
            val file = repo.downloadTo(submission, targetDir)
            opener(file)
        }.onFailure { _uploadState.value = Outcome.Error(it.userMessage("Could not open the file."), it) }
    }
}
