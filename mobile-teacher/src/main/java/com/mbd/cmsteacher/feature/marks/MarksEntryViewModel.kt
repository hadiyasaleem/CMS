package com.mbd.cmsteacher.feature.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.MarksEntryController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MarksEntryViewModel @Inject constructor(
    sessionManager: SessionManager,
    marksRepository: SessionMarksRepository,
    sessionRepository: AcademicSessionRepository,
    markEditRequestRepository: MarkEditRequestRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {

    val controller = MarksEntryController(
        marksRepository = marksRepository,
        sessionRepository = sessionRepository,
        markEditRequestRepository = markEditRequestRepository,
        teacherId = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val assignments = assignmentsProvider.observeMyAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
