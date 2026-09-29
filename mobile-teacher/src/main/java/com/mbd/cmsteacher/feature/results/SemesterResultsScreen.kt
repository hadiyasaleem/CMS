package com.mbd.cmsteacher.feature.results

import com.mbd.cmscommon.controller.ScopeFilterOptions
import com.mbd.cmscommon.controller.scopeDepartments
import com.mbd.cmscommon.controller.scopeSessions
import com.mbd.cmscommon.util.rememberDocumentExport
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SemesterResultsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.SemesterResultsWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class SemesterResultsViewModel @Inject constructor(
    marksRepository: SessionMarksRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {
    val controller = SemesterResultsController(
        marksRepository = marksRepository,
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        sessions = assignmentsProvider.observeMyAssignments()
            .map { assignments -> assignments.map { it.classKey to it.sessionLabel }.distinct() },
        scope = viewModelScope,
        filterOptions = assignmentsProvider.observeMyAssignments().map { ScopeFilterOptions(it.scopeDepartments(), it.scopeSessions()) },
    )
}

@Composable
fun SemesterResultsScreen(viewModel: SemesterResultsViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val sessions by controller.sessions.collectAsState()
    val visibleClasses by controller.visibleSessions.collectAsState()
    val filterScope by controller.filterScope.collectAsState()
    val filterOptions by controller.filterOptions.collectAsState()
    val sessionId by controller.sessionId.collectAsState()
    val semester by controller.semester.collectAsState()
    val roster by controller.roster.collectAsState()
    val results by controller.results.collectAsState()
    val subjects by controller.subjects.collectAsState()
    val saveState by controller.saveState.collectAsState()
    val loadState by controller.loadState.collectAsState()
    val semesterRange by controller.semesterRange.collectAsState()

    SemesterResultsWorkspace(

        onExport = rememberDocumentExport(),
        sessions = sessions,
        semesterRange = semesterRange,
        classOptions = visibleClasses,
        filterScope = filterScope,
        filterOptions = filterOptions,
        onFilterScope = controller::setFilterScope,
        sessionId = sessionId,
        semester = semester,
        roster = roster,
        results = results,
        subjects = subjects,
        saveOutcome = saveState,
        loadOutcome = loadState,
        onSelectSession = controller::selectSession,
        onSemester = controller::setSemester,
        onRetry = controller::refresh,
        onClearSave = controller::clearSave,
        onRecord = controller::record,
    )
}
