package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.SemesterResultsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.SemesterResultsWorkspace
import com.mbd.cmscommon.util.Outcome

/** Semester GPA / result recording leaf reachable from Exams hub. */
@Composable
fun SemesterResultsScreen(
    teacherId: String,
    sessionRepository: AcademicSessionRepository,
    marksRepository: SessionMarksRepository,
    curriculumRepository: CurriculumRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(marksRepository, sessionRepository, curriculumRepository, assignmentsProvider, teacherId) {
        SemesterResultsController(
            marksRepository,
            sessionRepository,
            curriculumRepository,
            assignmentsProvider.observeAssignmentsFor(teacherId),
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
        saveOutcome = saveState ?: Outcome.Success(Unit),
        loadOutcome = loadState ?: Outcome.Success(Unit),
        onSelectSession = controller::selectSession,
        onSemester = controller::setSemester,
        onRetry = controller::refresh,
        onClearSave = controller::clearSave,
        onRecord = controller::record,
    )
}
