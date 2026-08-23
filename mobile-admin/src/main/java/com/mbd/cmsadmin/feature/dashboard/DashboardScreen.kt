package com.mbd.cmsadmin.feature.dashboard

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
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmsadmin.R
import com.mbd.cmsadmin.navigation.AdminLeaf
import com.mbd.cmsadmin.navigation.AdminTab
import com.mbd.cmscommon.ui.components.AdminDashboardContent
import com.mbd.cmscommon.ui.components.DashboardActionUi

@Composable
fun DashboardScreen(onOpen: (String) -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val actions = listOf(
        DashboardActionUi("Departments", "Programs, sessions and curricula", Icons.Outlined.School) {
            onOpen(AdminTab.Academics.route)
        },
        DashboardActionUi("Teachers", "Faculty profiles and permissions", Icons.Outlined.Groups) {
            onOpen(AdminLeaf.TEACHERS)
        },
        DashboardActionUi("Calendar", "Events, holidays and deadlines", Icons.Outlined.CalendarMonth) {
            onOpen(AdminLeaf.CALENDAR)
        },
        DashboardActionUi("Link requests", "Connect student accounts", Icons.Outlined.HowToReg) {
            onOpen(AdminLeaf.LINK_REQUESTS)
        },
        DashboardActionUi("Insights", "Attendance and results overview", Icons.Outlined.Assessment) {
            onOpen(AdminLeaf.INSIGHTS)
        },
        DashboardActionUi("Notifications", "Publish targeted announcements", Icons.Outlined.Campaign) {
            onOpen(AdminLeaf.NOTIFICATIONS)
        },
    )

    AdminDashboardContent(
        state = state,
        heroPainter = painterResource(R.drawable.admin_dashboard_hero),
        actions = actions,
        onOpenMasterTimetable = { onOpen(AdminLeaf.MASTER_TIMETABLE) },
        onOpenLinkRequests = { onOpen(AdminLeaf.LINK_REQUESTS) },
        onOpenNotifications = { onOpen(AdminLeaf.NOTIFICATIONS) },
    )
}
