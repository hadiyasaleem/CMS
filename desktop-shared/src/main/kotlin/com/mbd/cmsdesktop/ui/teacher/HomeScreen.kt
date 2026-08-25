package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.model.teacherHomeSnapshot
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.TeacherHomeDestination
import com.mbd.cmscommon.ui.components.TeacherHomeWorkspace
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.combine

/**
 * Landing tab: combines this week's timetable periods with resolved assignments into a
 * [com.mbd.cmscommon.domain.model.TeacherHomeSnapshot] and dispatches the quick-action tiles to
 * the relevant [TeacherScreen].
 */
@Composable
fun HomeScreen(
    role: UserRole.Teacher,
    timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    onNavigate: (TeacherScreen) -> Unit,
) {
    val teacherId = role.teacherId
    val homeFlow = remember(teacherId, timetableRepository, assignmentsProvider) {
        combine(
            timetableRepository.observeMyPeriods(teacherId),
            assignmentsProvider.observeAssignmentsFor(teacherId),
        ) { periods, assignments ->
            teacherHomeSnapshot(teacherId, periods, assignments, LocalDate.now(), LocalTime.now())
        }
    }
    val snapshot by homeFlow.collectAsState(
        initial = teacherHomeSnapshot(teacherId, emptyList(), emptyList(), LocalDate.now(), LocalTime.now()),
    )

    TeacherHomeWorkspace(
        heroPainter = painterResource("teacher-home-hero.jpg"),
        snapshot = snapshot,
        onOpen = { destination ->
            onNavigate(
                when (destination) {
                    TeacherHomeDestination.ATTENDANCE -> TeacherScreen.Attendance
                    TeacherHomeDestination.MARKS -> TeacherScreen.Marks
                    TeacherHomeDestination.EXAM_PAPER -> TeacherScreen.ExamPaper
                    TeacherHomeDestination.STUDENTS -> TeacherScreen.MyStudents
                    TeacherHomeDestination.SCHEDULE -> TeacherScreen.Schedule
                    TeacherHomeDestination.NOTIFICATIONS -> TeacherScreen.Notifications
                },
            )
        },
    )
}
