package com.mbd.cmsteacher.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.ExamPaperSubmissionController
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExamPaperSubmissionViewModel @Inject constructor(
    sessionManager: SessionManager,
    repo: ExamPaperSubmissionRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {

    val controller = ExamPaperSubmissionController(
        repo = repo,
        teacherId = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val assignments = assignmentsProvider.observeMyAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
