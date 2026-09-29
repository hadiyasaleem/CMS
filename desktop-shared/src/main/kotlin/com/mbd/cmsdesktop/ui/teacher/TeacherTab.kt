package com.mbd.cmsdesktop.ui.teacher

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ClipboardCheck
import compose.icons.tablericons.Home
import compose.icons.tablericons.Menu2
import compose.icons.tablericons.Notebook
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 mobile-parity bottom-nav destinations for the teacher desktop shell. */
enum class TeacherTab(val label: String, val icon: ImageVector, val root: TeacherScreen) {
    Home("Home", TablerIcons.Home, TeacherScreen.Home),
    Attendance("Attend", TablerIcons.ClipboardCheck, TeacherScreen.Attendance),
    ExamsHub("Exams", TablerIcons.Notebook, TeacherScreen.ExamsHub),
    Schedule("Schedule", TablerIcons.Calendar, TeacherScreen.Schedule),
    MenuHub("Menu", TablerIcons.Menu2, TeacherScreen.MenuHub),
}
