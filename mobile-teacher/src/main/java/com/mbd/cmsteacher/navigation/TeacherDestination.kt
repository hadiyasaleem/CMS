package com.mbd.cmsteacher.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TeacherDestination(
    val route: String,
    val label: String,
    val navLabel: String = label,
    val navIcon: ImageVector? = null,
) {
    data object Home : TeacherDestination("home", "Home", "Home", Icons.Filled.Home)
    data object Attendance : TeacherDestination("attendance", "Mark Attendance", "Attend", Icons.Filled.FactCheck)
    data object AttendanceHistory : TeacherDestination("attendance_history/{sessionId}/{courseCode}/{shift}", "Attendance History")
    data object AttendanceStudent : TeacherDestination("attendance_student/{sessionId}/{courseCode}/{rollNumber}", "Student Attendance")
    data object ExamsHub : TeacherDestination("exams_hub", "Exams", "Exams", Icons.Filled.MenuBook)
    data object Marks : TeacherDestination("marks", "Marks Entry")
    data object SemesterResults : TeacherDestination("semester_results", "Semester Results")
    data object ExamPaper : TeacherDestination("exam_paper", "Submit Exam Paper")
    data object Schedule : TeacherDestination("schedule", "Schedule", "Schedule", Icons.Filled.CalendarMonth)
    data object MenuHub : TeacherDestination("menu_hub", "Menu", "Menu", Icons.Filled.Menu)
    data object Notifications : TeacherDestination("notifications", "Notifications")
    data object LinkRequests : TeacherDestination("link_requests", "Link Requests")
    data object MyStudents : TeacherDestination("my_students", "My Students")
    data object Events : TeacherDestination("events", "Events")
    data object Datesheets : TeacherDestination("datesheets", "Datesheets")
    data object Insights : TeacherDestination("insights", "Insights")
    data object Profile : TeacherDestination("profile", "Profile")

    companion object {
        val bottomNavItems = listOf(Home, Attendance, ExamsHub, Schedule, MenuHub)

        /** [shift] is the class's shift ("ALL" when unknown): the register lists that shift's students only. */
        fun attendanceHistory(sessionId: String, courseCode: String, shift: com.mbd.cmscommon.domain.model.Session?) =
            "attendance_history/$sessionId/$courseCode/${shift?.name ?: "ALL"}"

        fun attendanceStudent(sessionId: String, courseCode: String, rollNumber: String) =
            "attendance_student/$sessionId/$courseCode/$rollNumber"
    }
}
