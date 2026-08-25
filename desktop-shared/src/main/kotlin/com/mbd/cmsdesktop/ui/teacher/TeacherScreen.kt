package com.mbd.cmsdesktop.ui.teacher

/**
 * Navigation-state for the teacher desktop shell. [TeacherNavHost] keeps the current value in a
 * `mutableStateOf<TeacherScreen>` and swaps on it; [TeacherTab] entries point at the root screen
 * for each bottom-nav tab.
 */
sealed interface TeacherScreen {
    data object Home : TeacherScreen
    data object Attendance : TeacherScreen
    data class AttendanceHistory(val sessionId: String, val courseCode: String) : TeacherScreen
    data object ExamsHub : TeacherScreen
    data object Marks : TeacherScreen
    data object ExamPaper : TeacherScreen
    data object SemesterResults : TeacherScreen
    data object Schedule : TeacherScreen
    data object MenuHub : TeacherScreen
    data object Notifications : TeacherScreen
    data object LinkRequests : TeacherScreen
    data object MyStudents : TeacherScreen
    data object Calendar : TeacherScreen
    data object Datesheets : TeacherScreen
    data object Documents : TeacherScreen
    data object Insights : TeacherScreen
    data object Profile : TeacherScreen
}
