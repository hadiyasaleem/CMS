package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SemesterSubjectsController
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.ui.components.SemesterCurriculumWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SemesterSubjectsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    curriculumRepository: CurriculumRepository,
    sessionRepository: AcademicSessionRepository,
) : ViewModel() {
    private val controller = SemesterSubjectsController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        semester = checkNotNull(savedStateHandle.get<String>("semester")).toInt(),
        repo = curriculumRepository,
        sessionRepository = sessionRepository,
        scope = viewModelScope,
    )

    val sessionId = controller.sessionId
    val semester = controller.semester
    val subjects = controller.subjects
    val session = controller.session
    val term = controller.term
    val loading = controller.loading
    val error = controller.error
    val notice = controller.notice
    val termError = controller.termError

    fun saveTerm(start: String, end: String, onDone: (Boolean) -> Unit) = controller.saveTerm(start, end, onDone)
    fun saveSubject(originalCode: String?, code: String, name: String, credits: Int, type: SubjectType, elective: Boolean, outline: String?) =
        controller.saveSubject(originalCode, code, name, credits, type, elective, outline)
    fun removeSubject(courseCode: String) = controller.removeSubject(courseCode)
    fun clearError() = controller.clearError()
    fun clearTermError() = controller.clearTermError()
    fun consumeNotice() = controller.consumeNotice()
}

@Composable
fun SemesterSubjectsScreen(viewModel: SemesterSubjectsViewModel = hiltViewModel()) {
    val subjects by viewModel.subjects.collectAsState()
    val session by viewModel.session.collectAsState()
    val term by viewModel.term.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val termError by viewModel.termError.collectAsState()

    SemesterCurriculumWorkspace(
        sessionId = viewModel.sessionId,
        session = session,
        semester = viewModel.semester,
        subjects = subjects,
        term = term,
        loading = loading,
        errorMessage = errorMessage,
        notice = notice,
        onSaveSubject = viewModel::saveSubject,
        onRemoveSubject = viewModel::removeSubject,
        onSaveTerm = viewModel::saveTerm,
        onClearError = viewModel::clearError,
        onConsumeNotice = viewModel::consumeNotice,
        termError = termError,
        onClearTermError = viewModel::clearTermError,
    )
}
