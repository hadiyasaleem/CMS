package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.MarksEntryController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.MarksEntryWorkspace

/** Marks-entry leaf reachable from Home / Exams hub. */
@Composable
fun MarksEntryScreen(
    teacherId: String,
    sessionRepository: AcademicSessionRepository,
    marksRepository: SessionMarksRepository,
    markEditRequestRepository: MarkEditRequestRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(marksRepository, sessionRepository, markEditRequestRepository, teacherId) {
        MarksEntryController(marksRepository, sessionRepository, markEditRequestRepository, teacherId, scope)
    }
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
    val selected by controller.selected.collectAsState()
    val examType by controller.examType.collectAsState()
    val roster by controller.roster.collectAsState()
    val scores by controller.displayScores.collectAsState()
    val lockedRolls by controller.lockedRolls.collectAsState()
    val pendingByRoll by controller.pendingByRoll.collectAsState()
    val absentRolls by controller.absentRolls.collectAsState()
    val savedAbsentRolls by controller.savedAbsentRolls.collectAsState()
    val saveState by controller.saveState.collectAsState()
    val requestState by controller.requestState.collectAsState()

    MarksEntryWorkspace(
        assignments = assignments,
        selected = selected,
        examType = examType,
        roster = roster,
        scores = scores,
        lockedRolls = lockedRolls,
        pendingByRoll = pendingByRoll,
        absentRolls = absentRolls,
        savedAbsentRolls = savedAbsentRolls,
        saveOutcome = saveState,
        requestOutcome = requestState,
        onSelect = controller::select,
        onExamType = controller::selectExamType,
        onScore = controller::setScore,
        onToggleAbsent = controller::toggleAbsent,
        onSave = controller::save,
        onClearRequestState = controller::clearRequestState,
        onRequestEdit = controller::requestMarkEdit,
    )
}
