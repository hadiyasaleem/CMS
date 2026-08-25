package com.mbd.cmsteacher.feature.marks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.MarksEntryWorkspace
import com.mbd.cmscommon.util.Outcome

@Composable
fun MarksEntryScreen(viewModel: MarksEntryViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val assignments by viewModel.assignments.collectAsState()
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
        saveOutcome = saveState ?: Outcome.Success(Unit),
        requestOutcome = requestState ?: Outcome.Success(Unit),
        onSelect = controller::select,
        onExamType = controller::selectExamType,
        onScore = controller::setScore,
        onToggleAbsent = controller::toggleAbsent,
        onSave = controller::save,
        onClearRequestState = controller::clearRequestState,
        onRequestEdit = controller::requestMarkEdit,
    )
}
