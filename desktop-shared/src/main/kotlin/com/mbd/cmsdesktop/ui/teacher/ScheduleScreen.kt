package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.TeacherScheduleController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.TeacherScheduleWorkspace

/** Weekly timetable tab, backed by [TeacherScheduleController]. */
@Composable
fun ScheduleScreen(
    teacherId: String,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    timetableRepository: SessionTimetableRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(teacherId, departmentRepository, sessionRepository, timetableRepository) {
        TeacherScheduleController(teacherId, departmentRepository, sessionRepository, timetableRepository, scope)
    }
    val periods by controller.periods.collectAsState()
    val sessions by controller.sessions.collectAsState()
    val selectedDay by controller.selectedDay.collectAsState()
    val refreshState by controller.refreshState.collectAsState()

    TeacherScheduleWorkspace(
        heroPainter = painterResource("teacher-schedule-hero.jpg"),
        periods = periods,
        sessions = sessions,
        selectedDay = selectedDay,
        outcome = refreshState ?: com.mbd.cmscommon.util.Outcome.Success(Unit),
        onSelectDay = controller::selectDay,
        onRefresh = controller::refresh,
    )
}
