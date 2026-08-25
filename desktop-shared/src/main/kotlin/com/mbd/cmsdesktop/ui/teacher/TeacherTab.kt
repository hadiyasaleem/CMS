package com.mbd.cmsdesktop.ui.teacher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 mobile-parity bottom-nav destinations for the teacher desktop shell. */
enum class TeacherTab(val label: String, val icon: ImageVector, val root: TeacherScreen) {
    Home("Home", Icons.Filled.Home, TeacherScreen.Home),
    Attendance("Attend", Icons.Filled.FactCheck, TeacherScreen.Attendance),
    ExamsHub("Exams", Icons.Filled.MenuBook, TeacherScreen.ExamsHub),
    Schedule("Schedule", Icons.Filled.CalendarMonth, TeacherScreen.Schedule),
    MenuHub("Menu", Icons.Filled.Menu, TeacherScreen.MenuHub),
}
