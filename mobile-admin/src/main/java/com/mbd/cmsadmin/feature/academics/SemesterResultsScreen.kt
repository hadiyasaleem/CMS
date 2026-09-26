package com.mbd.cmsadmin.feature.academics

import com.mbd.cmscommon.controller.ScopeFilterOptions
import com.mbd.cmscommon.controller.shiftClassOptions
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
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.SemesterResultsWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine

@HiltViewModel
class SemesterResultsViewModel @Inject constructor(
    marksRepository: SessionMarksRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    val controller = SemesterResultsController(
        marksRepository = marksRepository,
        sessionRepository = sessionRepository,
        curriculumRepository = curriculumRepository,
        sessions = combine(sessionRepository.observeAllSessions(), departmentRepository.observeActiveDepartments()) { sessions, depts ->
            // One class per shift: results are recorded for that shift's students.
            shiftClassOptions(sessions) { session ->
                val deptName = depts.firstOrNull { it.deptId == session.deptId }?.name ?: session.deptId
                "$deptName ${session.label}"
            }
        },
        scope = viewModelScope,
        filterOptions = combine(departmentRepository.observeActiveDepartments(), sessionRepository.observeAllSessions()) { depts, sessions ->
            ScopeFilterOptions.of(depts, sessions)
        },
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

    SemesterResultsWorkspace(

        onExport = rememberDocumentExport(),
        sessions = sessions,
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
