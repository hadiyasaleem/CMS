package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.MarkAttendanceController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.MarkAttendanceWorkspace
import com.mbd.cmscommon.util.Outcome

/** Attendance tab: mark today's session, then jump into [AttendanceHistoryScreen] via [onOpenHistory]. */
@Composable
fun MarkAttendanceScreen(
    teacherId: String,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    notificationRepository: NotificationRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    onOpenHistory: (sessionId: String, courseCode: String) -> Unit = { _, _ -> },
) {
    val assignments by assignmentsProvider.observeAssignmentsFor(teacherId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val controller = remember(attendanceRepository, sessionRepository, notificationRepository, teacherId) {
        MarkAttendanceController(attendanceRepository, sessionRepository, notificationRepository, teacherId, scope)
    }
    val selected by controller.selected.collectAsState()
    val roster by controller.roster.collectAsState()
    val termPercents by controller.termPercents.collectAsState()
    val statuses by controller.statuses.collectAsState()
    val late by controller.late.collectAsState()
    val remarks by controller.remarks.collectAsState()
    val alreadyMarked by controller.alreadyMarked.collectAsState()
    val allMarked by controller.allMarked.collectAsState()
    val lectureTopic by controller.lectureTopic.collectAsState()
    val submitState by controller.submitState.collectAsState()

    MarkAttendanceWorkspace(
        heroPainter = painterResource("teacher-attendance-hero.jpg"),
        assignments = assignments,
        selected = selected,
        roster = roster,
        termPercents = termPercents,
        statuses = statuses,
        lateRolls = late,
        remarks = remarks,
        alreadyMarked = alreadyMarked,
        allMarked = allMarked,
        lectureTopic = lectureTopic,
        outcome = submitState ?: Outcome.Success(Unit),
        onSelect = controller::select,
        onStatus = controller::setStatus,
        onToggleLate = controller::toggleLate,
        onRemark = controller::setRemark,
        onLectureTopic = controller::setLectureTopic,
        onHistory = onOpenHistory,
        onSubmit = controller::submit,
    )
}
