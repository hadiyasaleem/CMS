package com.mbd.cmsadmin.feature.datesheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DatesheetsController
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.DatesheetWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatesheetsViewModel @Inject constructor(
    repo: DatesheetRepository,
    private val curriculumRepository: CurriculumRepository,
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    val controller = DatesheetsController(repo, createdBy = sessionManager.accountKey.orEmpty(), scope = viewModelScope)
    val sessions = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val invigilators = teacherRepository.observeActiveTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _subjectsBySession = MutableStateFlow<Map<String, List<SemesterSubject>>>(emptyMap())
    val subjectsBySession: StateFlow<Map<String, List<SemesterSubject>>> = _subjectsBySession
    private val loadingSubjects = mutableSetOf<String>()

    fun loadSubjects(sessionId: String) {
        if (sessionId in _subjectsBySession.value || !loadingSubjects.add(sessionId)) return
        viewModelScope.launch {
            try {
                runCatching { curriculumRepository.syncSession(sessionId) }
                _subjectsBySession.value += sessionId to curriculumRepository.observeSessionSubjects(sessionId).first()
            } finally {
                loadingSubjects -= sessionId
            }
        }
    }
}

@Composable
fun DatesheetsScreen(viewModel: DatesheetsViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val sheets by controller.sheets.collectAsState()
    val slots by controller.slots.collectAsState()
    val loadingSlots by controller.loadingSlots.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val subjects by viewModel.subjectsBySession.collectAsState()
    val invigilators by viewModel.invigilators.collectAsState()
    val loading by controller.refreshing.collectAsState()
    val busy by controller.busy.collectAsState()
    val error by controller.error.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()

    DatesheetWorkspace(
        sheets = sheets,
        slots = slots,
        loadingSlots = loadingSlots,
        sessions = sessions,
        subjectsBySession = subjects,
        invigilators = invigilators,
        viewer = DatesheetViewerContext(DatesheetViewerRole.ADMIN),
        loading = loading,
        busy = busy,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onLoadSlots = controller::loadSlots,
        onLoadSubjects = viewModel::loadSubjects,
        onCreate = controller::createDatesheet,
        onUpdate = controller::updateDatesheet,
        onSetPublished = controller::setPublished,
        onDelete = controller::deleteDatesheet,
        onAddSlot = controller::addSlot,
        onUpdateSlot = controller::updateSlot,
        onDeleteSlot = controller::deleteSlot,
    )
}
