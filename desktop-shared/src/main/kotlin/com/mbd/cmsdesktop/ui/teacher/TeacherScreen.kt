package com.mbd.cmsdesktop.ui.teacher

import com.mbd.cmscommon.domain.model.Session
import java.time.YearMonth

/**
 * Navigation-state for the teacher desktop shell. [TeacherNavHost] keeps the current value in a
 * `mutableStateOf<TeacherScreen>` and swaps on it; [TeacherTab] entries point at the root screen
 * for each bottom-nav tab.
 */
sealed interface TeacherScreen {
    data object Home : TeacherScreen
    data object Attendance : TeacherScreen
    /** [shift] is the class's shift: the register lists that shift's students only (null = whole session). */
    data class AttendanceHistory(val sessionId: String, val courseCode: String, val month: YearMonth? = null, val shift: Session? = null) : TeacherScreen
    data class AttendanceStudentSummary(val sessionId: String, val courseCode: String, val rollNumber: String, val returnMonth: YearMonth, val returnShift: Session? = null, val historySessionIds: String = sessionId) : TeacherScreen
    data object ExamsHub : TeacherScreen
    data object Marks : TeacherScreen
    data object ExamPaper : TeacherScreen
    data object Schedule : TeacherScreen
    data object MenuHub : TeacherScreen
    data object Notifications : TeacherScreen
    data object LinkRequests : TeacherScreen
    data object MyStudents : TeacherScreen
    data object Calendar : TeacherScreen
    data object Datesheets : TeacherScreen
    data object Insights : TeacherScreen
    data object Profile : TeacherScreen
}

/** The top bar's title for this screen: "GGC-MBD" (the CmsTopBar default, unchanged from today)
 * on a tab root, each screen's own short name everywhere else. */
fun TeacherScreen.title(): String = when (this) {
    TeacherScreen.Home, TeacherScreen.Attendance, TeacherScreen.ExamsHub,
    TeacherScreen.Schedule, TeacherScreen.MenuHub,
    -> "GGC-MBD"
    is TeacherScreen.AttendanceHistory -> "Attendance History"
    is TeacherScreen.AttendanceStudentSummary -> "Student Attendance"
    TeacherScreen.Marks -> "Marks Entry"
    TeacherScreen.ExamPaper -> "Submit Exam Paper"
    TeacherScreen.Notifications -> "Notifications"
    TeacherScreen.LinkRequests -> "Link Requests"
    TeacherScreen.MyStudents -> "My Students"
    TeacherScreen.Calendar -> "Calendar"
    TeacherScreen.Datesheets -> "Datesheets"
    TeacherScreen.Insights -> "Insights"
    TeacherScreen.Profile -> "Profile"
}
