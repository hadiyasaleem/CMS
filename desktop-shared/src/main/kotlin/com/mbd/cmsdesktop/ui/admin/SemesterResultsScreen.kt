package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SemesterResultsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.SemesterResultsWorkspace
import kotlinx.coroutines.flow.combine

/** Semester GPA / result recording leaf reachable from Records hub -- covers every session, not
 * just a teacher's assigned ones. */
@Composable
fun SemesterResultsScreen(
    sessionRepository: AcademicSessionRepository,
    marksRepository: SessionMarksRepository,
    curriculumRepository: CurriculumRepository,
    departmentRepository: DepartmentRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(marksRepository, sessionRepository, curriculumRepository, departmentRepository) {
        SemesterResultsController(
            marksRepository,
            sessionRepository,
            curriculumRepository,
            combine(sessionRepository.observeAllSessions(), departmentRepository.observeActiveDepartments()) { sessions, depts ->
                sessions.map { session ->
                    val deptName = depts.firstOrNull { it.deptId == session.deptId }?.name ?: session.deptId
                    session.sessionId to "$deptName ${session.label}"
                }
            },
            scope,
        )
    }
    val sessions by controller.sessions.collectAsState()
    val sessionId by controller.sessionId.collectAsState()
    val semester by controller.semester.collectAsState()
    val roster by controller.roster.collectAsState()
    val results by controller.results.collectAsState()
    val subjects by controller.subjects.collectAsState()
    val loadState by controller.loadState.collectAsState()
    val saveState by controller.saveState.collectAsState()

    SemesterResultsWorkspace(
        sessions = sessions,
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
