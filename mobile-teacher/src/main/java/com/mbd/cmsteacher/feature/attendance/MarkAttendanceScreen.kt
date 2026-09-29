package com.mbd.cmsteacher.feature.attendance

import com.mbd.cmscommon.ui.components.ControllerErrorDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.MarkAttendanceWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmsteacher.R
import com.mbd.cmsteacher.navigation.TeacherDestination

@Composable
fun MarkAttendanceScreen(onOpenHistory: (String) -> Unit, viewModel: MarkAttendanceViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val assignments by viewModel.assignments.collectAsState()
    val selected by controller.selected.collectAsState()
    val roster by controller.roster.collectAsState()
    val termPercents by controller.termPercents.collectAsState()
    val statuses by controller.statuses.collectAsState()
    val lateRolls by controller.late.collectAsState()
    val remarks by controller.remarks.collectAsState()
    val alreadyMarked by controller.alreadyMarked.collectAsState()
    val lockedStudentIds by controller.lockedStudentIds.collectAsState()
    val allMarked by controller.allMarked.collectAsState()
    val lectureTopic by controller.lectureTopic.collectAsState()
    val submitState by controller.submitState.collectAsState()
    val date by controller.date.collectAsState()
    val topics by controller.topics.collectAsState()

    ControllerErrorDialog(controller.error, "Couldn't complete the attendance action", controller::clearError)

    MarkAttendanceWorkspace(
        heroPainter = painterResource(R.drawable.teacher_attendance_hero),
        assignments = assignments,
        selected = selected,
        roster = roster,
        termPercents = termPercents,
        statuses = statuses,
        lateRolls = lateRolls,
        remarks = remarks,
        alreadyMarked = alreadyMarked,
        lockedStudentIds = lockedStudentIds,
        allMarked = allMarked,
        lectureTopic = lectureTopic,
        outcome = submitState,
        onSelect = controller::select,
        onStatus = controller::setStatus,
        onToggleLate = controller::toggleLate,
        onRemark = controller::setRemark,
        onLectureTopic = controller::setLectureTopic,
        onHistory = { sessionId, courseCode ->
            onOpenHistory(TeacherDestination.attendanceHistory(sessionId, courseCode, viewModel.controller.selected.value?.classShift))
        },
        onSubmit = controller::submit,
        date = date,
        topics = topics,
        onToggleTopic = controller::toggleTopic,
        onDate = controller::setDate,
    )
}
