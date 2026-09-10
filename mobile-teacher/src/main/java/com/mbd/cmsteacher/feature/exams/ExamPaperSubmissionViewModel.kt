package com.mbd.cmsteacher.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.ExamPaperSubmissionController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExamPaperSubmissionViewModel @Inject constructor(
    sessionManager: SessionManager,
    repo: ExamPaperSubmissionRepository,
    datesheetRepository: DatesheetRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    academicSessionRepository: AcademicSessionRepository,
) : ViewModel() {

    val controller = ExamPaperSubmissionController(
        examPaperRepository = repo,
        datesheetRepository = datesheetRepository,
        assignmentsProvider = assignmentsProvider,
        sessionRepository = academicSessionRepository,
        teacherId = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )
}
