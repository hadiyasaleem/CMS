package com.mbd.cmsstudent.navigation

import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ClipboardCheck
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Home
import compose.icons.tablericons.Menu2
import compose.icons.tablericons.Report
import compose.icons.tablericons.User
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentDestination(
    val route: String,
    val label: String,
    val navIcon: ImageVector? = null,
    val navLabel: String = label,
) {
    data object Home : StudentDestination("home", "Home", TablerIcons.Home)
    data object Attendance : StudentDestination("attendance", "Attendance", TablerIcons.ClipboardCheck, "Attend")
    data object ExamsHub : StudentDestination("exams_hub", "Exams", TablerIcons.Report)
    data object Timetable : StudentDestination("timetable", "Timetable", TablerIcons.Calendar)
    data object More : StudentDestination("more", "More", TablerIcons.Menu2)
    data object Marks : StudentDestination("marks", "Marks", TablerIcons.Report)
    data object Results : StudentDestination("results", "Results", TablerIcons.Report)
    data object Events : StudentDestination("events", "Events", TablerIcons.Calendar)
    data object Datesheets : StudentDestination("datesheets", "Datesheets", TablerIcons.Calendar)
    data object Fees : StudentDestination("fees", "Fee Challan", TablerIcons.CreditCard)
    data object Notifications : StudentDestination("notifications", "Notifications", TablerIcons.Bell)
    data object Profile : StudentDestination("profile", "Profile", TablerIcons.User)

    companion object {
        val bottomNavItems = listOf(Home, Attendance, ExamsHub, Timetable, More)
    }
}
