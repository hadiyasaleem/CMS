package com.mbd.cmsdesktop.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.School
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
        DashboardActionUi("Departments", "Programs, sessions and curricula", Icons.Outlined.School, onOpenAcademics),
        DashboardActionUi("Teachers", "Faculty profiles and permissions", Icons.Outlined.Groups, onOpenTeachers),
        DashboardActionUi("Calendar", "Events, holidays and deadlines", Icons.Outlined.CalendarMonth, onOpenCalendar),
        DashboardActionUi("Link requests", "Connect student accounts", Icons.Outlined.HowToReg, onOpenLinkRequests),
        DashboardActionUi("Insights", "Attendance and results overview", Icons.Outlined.Assessment, onOpenInsights),
        DashboardActionUi("Notifications", "Publish targeted announcements", Icons.Outlined.Campaign, onOpenNotifications),
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
