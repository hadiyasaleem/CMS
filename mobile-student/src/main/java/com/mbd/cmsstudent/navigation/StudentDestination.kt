package com.mbd.cmsstudent.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class StudentDestination(
    val route: String,
    val label: String,
    val navIcon: ImageVector? = null,
    val navLabel: String = label,
) {
    data object Home : StudentDestination("home", "Home", Icons.Filled.Home)
    data object Attendance : StudentDestination("attendance", "Attendance", Icons.Filled.FactCheck, "Attend")
    data object ExamsHub : StudentDestination("exams_hub", "Exams", Icons.Filled.Grading)
    data object Timetable : StudentDestination("timetable", "Timetable", Icons.Filled.CalendarMonth)
    data object More : StudentDestination("more", "More", Icons.Filled.Menu)
    data object Marks : StudentDestination("marks", "Marks", Icons.Filled.Grading)
    data object Results : StudentDestination("results", "Results", Icons.Filled.Grading)
    data object Events : StudentDestination("events", "Events", Icons.Filled.CalendarMonth)
    data object Datesheets : StudentDestination("datesheets", "Datesheets", Icons.Filled.CalendarMonth)
    data object Fees : StudentDestination("fees", "Fee Challan", Icons.Filled.Payments)
    data object Notifications : StudentDestination("notifications", "Notifications", Icons.Filled.Notifications)
    data object Profile : StudentDestination("profile", "Profile", Icons.Filled.Person)

    companion object {
        val bottomNavItems = listOf(Home, Attendance, ExamsHub, Timetable, More)
    }
}
