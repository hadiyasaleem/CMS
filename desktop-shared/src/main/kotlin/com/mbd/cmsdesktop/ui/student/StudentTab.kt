package com.mbd.cmsdesktop.ui.student

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.Certificate
import compose.icons.tablericons.ClipboardCheck
import compose.icons.tablericons.Home
import compose.icons.tablericons.Menu2
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 bottom-nav-parity tabs of the student desktop shell (mirrors `StudentDestination.bottomNavItems` in mobile-student). */
enum class StudentTab(val label: String, val icon: ImageVector, val root: StudentScreen) {
    Home("Home", TablerIcons.Home, StudentScreen.Home),
    Attendance("Attend", TablerIcons.ClipboardCheck, StudentScreen.Attendance),
    ExamsHub("Exams", TablerIcons.Certificate, StudentScreen.ExamsHub),
    Timetable("Timetable", TablerIcons.Calendar, StudentScreen.Timetable),
    More("More", TablerIcons.Menu2, StudentScreen.MoreHub),
}
