package com.mbd.cmsdesktop.ui.admin

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.School
import compose.icons.tablericons.Speakerphone
import compose.icons.tablericons.UserCheck
import compose.icons.tablericons.Users
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.mbd.cmscommon.controller.DashboardController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.AdminDashboardContent
import com.mbd.cmscommon.ui.components.DashboardActionUi

@Composable
fun DashboardScreen(
    departmentRepository: DepartmentRepository,
    teacherRepository: TeacherRepository,
    sessionRepository: AcademicSessionRepository,
    linkRequestRepository: StudentLinkRequestRepository,
    onOpenAcademics: () -> Unit,
    onOpenTeachers: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenMasterTimetable: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionRepository, teacherRepository, departmentRepository, linkRequestRepository) {
        DashboardController(sessionRepository, teacherRepository, departmentRepository, linkRequestRepository, scope)
    }
    val state by controller.state.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val actions = listOf(
        DashboardActionUi("Departments", TablerIcons.School, onOpenAcademics),
        DashboardActionUi("Teachers", TablerIcons.Users, onOpenTeachers),
        DashboardActionUi("Calendar", TablerIcons.Calendar, onOpenCalendar),
        DashboardActionUi("Link requests", TablerIcons.UserCheck, onOpenLinkRequests),
        DashboardActionUi("Insights", TablerIcons.ChartBar, onOpenInsights),
        DashboardActionUi("Notifications", TablerIcons.Speakerphone, onOpenNotifications),
    )

    AdminDashboardContent(
        state = state,
        heroPainter = painterResource("admin-dashboard-hero.png"),
        actions = actions,
        onOpenMasterTimetable = onOpenMasterTimetable,
        onOpenLinkRequests = onOpenLinkRequests,
        onOpenNotifications = onOpenNotifications,
        errorMessage = errorMessage,
    )
}
