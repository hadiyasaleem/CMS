package com.mbd.cmsadmin.feature.dashboard

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
        DashboardActionUi("Departments", TablerIcons.School) {
            onOpen(AdminTab.Academics.route)
        },
        DashboardActionUi("Teachers", TablerIcons.Users) {
            onOpen(AdminLeaf.TEACHERS)
        },
        DashboardActionUi("Calendar", TablerIcons.Calendar) {
            onOpen(AdminLeaf.CALENDAR)
        },
        DashboardActionUi("Link requests", TablerIcons.UserCheck) {
            onOpen(AdminLeaf.LINK_REQUESTS)
        },
        DashboardActionUi("Insights", TablerIcons.ChartBar) {
            onOpen(AdminLeaf.INSIGHTS)
        },
        DashboardActionUi("Notifications", TablerIcons.Speakerphone) {
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
