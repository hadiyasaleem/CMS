package com.mbd.cmsdesktop.ui.student

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 bottom-nav-parity tabs of the student desktop shell (mirrors `StudentDestination.bottomNavItems` in mobile-student). */
enum class StudentTab(val label: String, val icon: ImageVector, val root: StudentScreen) {
    Home("Home", Icons.Filled.Home, StudentScreen.Home),
    Attendance("Attend", Icons.Filled.FactCheck, StudentScreen.Attendance),
    ExamsHub("Exams", Icons.Filled.Grading, StudentScreen.ExamsHub),
    Timetable("Timetable", Icons.Filled.CalendarMonth, StudentScreen.Timetable),
    More("More", Icons.Filled.Menu, StudentScreen.MoreHub),
}
